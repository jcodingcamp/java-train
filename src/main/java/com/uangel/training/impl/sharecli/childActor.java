package com.uangel.training.impl.sharecli;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import com.uangel.training.actor.util.ResponseType;
import com.uangel.training.ctclient.ClientConnection;
import com.uangel.training.ctclient.ClientStatusListener;
import io.netty.channel.EventLoopGroup;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class childActor extends AbstractActorWithStash implements ClientStatusListener {

    private messageNewClient spec;
    private EventLoopGroup workerGroup;


    private Cancellable cancelTimer = getContext().getSystem().getScheduler().scheduleWithFixedDelay(Duration.ofSeconds(5), Duration.ofSeconds(1), self(), new messageConnCheck(), getContext().dispatcher(),  ActorRef.noSender());


    // 일정시간 동안 message 가 없으면 자동으로 종료되게 , 마지만 message 받은 시간을 기록
    OffsetDateTime lastMessageTime = OffsetDateTime.now();


    @Override
    public void postStop()  {
        System.out.println("child actor stopped");
        cancelTimer.cancel();
        connections.forEach(ClientConnection::close);
        connections = new ArrayList<>();
    }

    public childActor(messageNewClient req, EventLoopGroup workerGroup) {
        this.spec = req;
        this.workerGroup = workerGroup;



        for(int i=0;i< req.numConnection;i++) {
            ClientConnection.newConnection(workerGroup,this,req.addr, req.port).whenComplete((clientConnection, throwable) -> {
                if (throwable != null ) {
                    self().tell(new messageConnectFailed(throwable), ActorRef.noSender());
                }
            });

        }
        getContext().become(new initialState().createReceive());

        //getContext().setReceiveTimeout(Duration.ofSeconds(10));
    }

    public static Props props(messageNewClient req, EventLoopGroup workerGroup) {
        return Props.create(childActor.class, () -> new childActor(req, workerGroup));
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    class initialState {

        int failCount = 0;
        public initialState() {

        }

        void onConnected( messageConnected r) {
            unstashAll();
            connections.add(r.conn);
            getContext().become(new activeState().createReceive() );
            r.sendResponse(sender(), true , self());
        }

        void onSendRequest( messageSendRequest r) {
            // 마지막으로 message 받은 시각을 저장
            lastMessageTime = OffsetDateTime.now();
            stash();
        }

        void onClose( messageClose r ) {
            getContext().stop(self());
        }

        void onConnectFailed( messageConnectFailed r) {
            failCount++;
            if (failCount == spec.numConnection) {
                unstashAll();
                getContext().become(new activeState().createReceive());
            }
        }

        public Receive createReceive() {
            return receiveBuilder()
                    .match(messageSendRequest.class, this::onSendRequest)
                    .match(messageConnected.class, this::onConnected)
                    .match(messageClose.class, this::onClose)
                    .match(messageConnectFailed.class , this::onConnectFailed)
                    .matchAny(r -> System.out.println("unexpected message : " + r))
                    .build();
        }
    }



    private CompletableFuture<String> sendRequestTo(List<ClientConnection> connections, int idx, String msg ) {
        if (connections.size() == 0 ) {
            return CompletableFuture.failedFuture(new NoSuchElementException("no connection"));
        }

        var ret =  connections.get(idx).sendRequest(msg);
        return ret.handle((s, throwable) -> {
            if (throwable != null ) {
                if (connections.size() > idx +1 ) {
                    return sendRequestTo(connections,idx+1, msg);
                } else {
                    return CompletableFuture.<String>failedFuture(throwable);
                }
            }
            return CompletableFuture.completedFuture(s);
        }).thenCompose(x -> x);
    }

    List<ClientConnection> connections = new ArrayList<>();

    class activeState {
        void onSendRequest( messageSendRequest r) {
            // 마지막으로 메시지 받은 시각을 저장
            lastMessageTime = OffsetDateTime.now();

            System.out.println("onSendRequest");

            r.sendFutureResponse(sender(), sendRequestTo(connections, 0 , r.msg) , self());
        }

        void onConnected( messageConnected r) {
            System.out.println("on Connected");

            var newlist = new ArrayList<>(connections);
            newlist.add(r.conn);

            connections = newlist;

            r.sendResponse(sender(), true , self());
        }

        void onDisconnected(messageDisconnected r) {

            connections = connections.stream()
                    .filter(c -> c!=r.conn)
                    .collect(Collectors.toList());

            getContext().getSystem().getScheduler().scheduleOnce(Duration.ofSeconds(1), self(), new messageReconnect(), context().dispatcher(), ActorRef.noSender());
        }

        void onReconnect(messageReconnect r) {
            if (connections.size() < spec.numConnection) {
                ClientConnection.newConnection(workerGroup, childActor.this, spec.addr, spec.port);
            }
        }

        void onClose(messageClose r) {
            getContext().stop(self());
        }

        void onConnCheck(messageConnCheck r) {

            System.out.println("on conn check");
            var now = OffsetDateTime.now();
            var diff = Duration.between(lastMessageTime, now);

            // 마지막으로 메시지 받은 시각과 지금 시간을 비교하여 10초 동안 아무런 send request 를 받지 않았으면
            // actor stop
            if ( diff.getSeconds() >= 10 ) {
                getContext().stop(self());
                return;
            }

            if (connections.size() < spec.numConnection) {
                for(int i=0;i< spec.numConnection - connections.size();i++) {
                    System.out.println("create new connection");
                    self().tell(new messageReconnect(), ActorRef.noSender());
                }
            }
        }

        public Receive createReceive() {
            return receiveBuilder()
                    .match(messageSendRequest.class, this::onSendRequest)
                    .match(messageConnected.class, this::onConnected)
                    .match(messageDisconnected.class, this::onDisconnected)
                    .match(messageReconnect.class, this::onReconnect)
                    .match(messageClose.class, this::onClose)
                    .match(messageConnCheck.class , this::onConnCheck)
                    .matchAny(r -> System.out.println("unexpected message : " + r))
                    .build();
        }


    }



    @Override
    public void connected(ClientConnection conn) {
        ResponseType
                .askFor(self(), new messageConnected(conn), Duration.ofSeconds(3))
                .whenComplete((aBoolean, throwable) -> {
                    if (throwable!=null) {
                        conn.close();
                    }
                });
        //self().tell(new messageConnected(conn), ActorRef.noSender());
    }

    @Override
    public void disconnected(ClientConnection conn) {
        self().tell(new messageDisconnected(conn), ActorRef.noSender() );
    }

    private static class messageConnected implements ResponseType<Boolean> {
        private ClientConnection conn;

        public messageConnected(ClientConnection conn) {
            this.conn = conn;
        }
    }

    private static class messageDisconnected {
        private ClientConnection conn;

        public messageDisconnected(ClientConnection conn) {
            this.conn = conn;
        }
    }

    private static class messageReconnect {
    }

    private static class messageConnCheck {
    }

    private class messageConnectFailed {
        private Throwable throwable;

        public messageConnectFailed(Throwable throwable) {
            this.throwable = throwable;
        }
    }
}
