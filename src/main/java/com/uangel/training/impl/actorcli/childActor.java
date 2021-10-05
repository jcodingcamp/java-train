package com.uangel.training.impl.actorcli;

import akka.actor.*;
import com.uangel.training.actor.util.ResponseType;
import com.uangel.training.ctclient.ClientConnection;
import com.uangel.training.ctclient.ClientStatusListener;
import io.netty.channel.EventLoopGroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


// client actor
class childActor extends AbstractActorWithStash implements ClientStatusListener {

    private messageNewClient spec;
    private EventLoopGroup workerGroup;


    // 주기 적으로 ,  가지고 있는 connection 이 지정한 connection 수에 도달하도록 체크하기 위해
    // scheduler 를 이용 자기 자신에게 messageConnCheck 라는 메시지를 보냄
    private Cancellable cancelTimer = getContext().getSystem().getScheduler().scheduleWithFixedDelay(Duration.ofSeconds(5), Duration.ofSeconds(5), self(), new messageConnCheck(), getContext().dispatcher(),  ActorRef.noSender());

    @Override
    // child actor 가 종료될 때 , 모든 connection 을 끊고
    // timer 중단
    public void postStop()  {
        System.out.println("child actor stopped");
        cancelTimer.cancel();
        connections.forEach(ClientConnection::close);
        connections = new ArrayList<>();
    }

    // child actor 생성자
    public childActor(messageNewClient req, EventLoopGroup workerGroup) {
        this.spec = req;
        this.workerGroup = workerGroup;


        // 지정된 연결 개수 만큼 연결을 시도하고
        for(int i=0;i< req.numConnection;i++) {
            ClientConnection.newConnection(workerGroup,this,req.addr, req.port).whenComplete((clientConnection, throwable) -> {
                if (throwable != null ) {
                    self().tell(new messageConnectFailed(throwable), ActorRef.noSender());
                }
            });
        }

        // 하나라도 연결이 되거나, 모두 연결이 실패할 때까지 initialState 에 머므른다
        getContext().become(new initialState().createReceive());
    }

    public static Props props(messageNewClient req, EventLoopGroup workerGroup) {
        return Props.create(childActor.class, () -> new childActor(req, workerGroup));
    }

    @Override
    // 기본 receive 메소드는 사용하지 않음. become 을 이용하여 initialState 부터 시작
    public Receive createReceive() {
        return null;
    }

    // initialState
    // 이 state 에서는 모든 send request 를 stash 하고
    // 연결이 될 때까지 기다린다
    class initialState {

        int failCount = 0;
        public initialState() {

        }

        // messageSendRequest 를 받았을 때는 모든 메세지를 stash 함
        void onSendRequest( messageSendRequest r) {
            stash();
        }


        // messageConnected 를 받았을 때
        void onConnected( messageConnected r) {
            // unstashAll 을 호출 하여 모든 send request 메시지를 다시 처리되게 하고
            unstashAll();

            // connection 목록에 추가
            connections.add(r.conn);

            // active 상태로 이동
            getContext().become(new activeState().createReceive() );

            // 메시지 받았다는 응답을 sender 에게 전송
            r.sendResponse(sender(), true , self());
        }


        // messageClose 를 받으면 stop self 호출
        void onClose( messageClose r ) {
            getContext().stop(self());
        }

        // 모든 연결이 실패했을때
        // messageAllFailed 메시지를 받게 된다
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



    // idx 번째 connection에 request 를 보내고 , 실패했을 때 idx+1 번째로 request 를  재귀적으로 보내는 코드
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


    // active 상태
    class activeState {

        // messageSendRequest 를 받았을 때
        void onSendRequest( messageSendRequest r) {
            System.out.println("onSendRequest");

            // sender 에게 future 를 전송
            r.sendFutureResponse(sender(), sendRequestTo(connections, 0 , r.msg) , self());

            // 만약 future의 callback  안에서 sendResponse 를 호출 하려 한다면 ,
            // future 의 callback 에서는 sender 를 참조하면 안되기 때문에
            // 다음과 같이 미리 sender() 값을 저장해 두고 사용해야 함
//                    var returnPath = this.sender();
//                    sendRequestTo(connections, 0 , r.msg).whenComplete((s, throwable) -> {
//                        if (throwable != null) {
//                            r.sendResponse(returnPath, throwable, self());
//                        } else {
//                            r.sendResponse(returnPath, s, self());
//                        }
//                    });
        }

        // messageConnected 를 받았을 때
        void onConnected( messageConnected r) {
            System.out.println("on Connected");

            // immutable list 처럼 동작하게,  list 를 copy 하여 add
            var newlist = new ArrayList<>(connections);
            newlist.add(r.conn);

            connections = newlist;

            // connections 에 추가 되었다는 것을 sender 에게 전송
            r.sendResponse(sender(), true , self());
        }

        // messageDisconnected 를 받았을 때
        void onDisconnected(messageDisconnected r) {

            // connections 에서 삭제
            connections = connections.stream()
                    .filter(c -> c!=r.conn)
                    .collect(Collectors.toList());

            // 끊어진 연결을 재연결하도록 1초 뒤에 messageReconnect() 메시지 전송
            getContext().getSystem().getScheduler().scheduleOnce(Duration.ofSeconds(1), self(), new messageReconnect(), context().dispatcher(), ActorRef.noSender());
        }

        // messageReconnect 를 받았을 때 재연결
        void onReconnect(messageReconnect r) {
            if (connections.size() < spec.numConnection) {
                ClientConnection.newConnection(workerGroup, childActor.this, spec.addr, spec.port);
            }
        }

        void onClose(messageClose r) {
            getContext().stop(self());
        }

        void onConnCheck(messageConnCheck r) {
            if (connections.size() < spec.numConnection) {
                for(int i=0;i< spec.numConnection - connections.size();i++) {
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
    // connection 이 연결 되었을 때
    public void connected(ClientConnection conn) {
        ResponseType
                .askFor(self(), new messageConnected(conn), Duration.ofSeconds(3))
                .whenComplete((aBoolean, throwable) -> {
                    // 에러가 발생하여 actor의 connections 에 추가되지 못했을 경우
                    // connection 을 close 한다
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
