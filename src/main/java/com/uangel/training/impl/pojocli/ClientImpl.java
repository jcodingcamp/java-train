package com.uangel.training.impl.pojocli;

import com.uangel.training.ctclient.Client;
import com.uangel.training.ctclient.ClientConnection;
import com.uangel.training.ctclient.ClientStatusListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


// Client 의 구현
// actor 를 쓰지 않고 구현함
public class ClientImpl implements Client, ClientStatusListener {
    EventLoopGroup workerGroup = new NioEventLoopGroup();

    volatile List<CompletableFuture<ClientConnection>> connections = new ArrayList<>();

    private String addr;
    private int port;

    public ClientImpl(String addr, int port) {
        this.addr = addr;
        this.port = port;

        checkConns();

    }

    @Override
    public void close() {


        connections.stream().forEach(f -> f.thenAccept(c -> c.close()));


        workerGroup.shutdownGracefully();

    }


    // 주기적으로 connection 체크 해서 연결 실패한 연결을 다시 연결하는 코드
    public void checkConns() {

        connections = connections.stream().map(f -> {
            if ( f.isDone() && (f.isCompletedExceptionally() || f.isCancelled()) ){
                System.out.println("try reconnect");
                return ClientConnection.newConnection(this.workerGroup, this, this.addr, this.port);
            }
            return f;
        }).collect(Collectors.toList());

        CompletableFuture.delayedExecutor(5 , TimeUnit.SECONDS).execute(() -> {
            if (!workerGroup.isShutdown()) {
                checkConns();
            }
        });

    }

    @Override
    public void connected(ClientConnection conn) {

    }

    @Override
    // 연결이 끊어진 경우 , 다시 연결하는 코드
    public void disconnected(ClientConnection conn) {
        connections = connections.stream().map(f -> {
            if ( f.isDone() && !f.isCompletedExceptionally() && !f.isCancelled()) {
                try {
                    var c = f.get();
                    if ( c == conn) {
                        var newf = new CompletableFuture<ClientConnection>();
                        CompletableFuture.delayedExecutor(1 , TimeUnit.SECONDS).execute(() -> {
                            var cf = ClientConnection.newConnection(this.workerGroup, this, this.addr, this.port);
                            cf.whenComplete((clientConnection, throwable) -> {
                                if(throwable != null) {
                                    newf.completeExceptionally(throwable);
                                } else {
                                    newf.complete(clientConnection);
                                }
                            });
                        });
                        return newf;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return f;
        }).collect(Collectors.toList());

    }

    // request 를 idx 번째 connection 에 보내고 실패하면
    // idx+1  번째에 재귀적으로 request 를 보내는 코드
    private CompletableFuture<String> sendRequestTo( int idx, String msg ) {
       var ret =  connections.get(idx).thenCompose(c -> c.sendRequest(msg));
       return ret.handle((s, throwable) -> {
            if (throwable != null ) {
                if (connections.size() > idx +1 ) {
                    return sendRequestTo(idx+1, msg);
                } else {
                    return CompletableFuture.<String>failedFuture(throwable);
                }
            }
            return CompletableFuture.completedFuture(s);
       }).thenCompose(x -> x);
    }


    @Override
    public CompletableFuture<String> sendRequest(String msg) {

        if(connections.size() > 0) {
            return sendRequestTo(0, msg);
        }
        return CompletableFuture.failedFuture(new NoSuchElementException("no connection"));
    }
}
