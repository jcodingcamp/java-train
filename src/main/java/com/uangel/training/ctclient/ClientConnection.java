package com.uangel.training.ctclient;

import com.uangel.training.ctmessage.CtDecoder;
import com.uangel.training.ctmessage.CtEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;

// netty connection
public class ClientConnection {

    private Channel channel;
    private ClientStatusListener client;

    public ClientConnection(ClientStatusListener client) {
        this.client = client;
    }


    public CompletableFuture<String> sendRequest( String msg ) {
        // response 에 대한 promise 생성
        CompletableFuture<String> promise = new CompletableFuture<>();

        // channel 에 write 할때  promise 와 msg 를 같이 전송
        // channel 에 write 하면  pipeline 의 마지막 handler의 write 가 호출됨
        // 여기서는 ClientHandler 가 마지막 handler 임
        channel.writeAndFlush(new Request( msg , promise ));

        return promise;
    }

    // 새로운 connection 을 만드는 함수
    public static CompletableFuture<ClientConnection> newConnection(EventLoopGroup workerGroup,  ClientStatusListener client, String addr , int port) {
        ClientConnection connection = new ClientConnection(client);
        CompletableFuture<ClientConnection> ret = new CompletableFuture<>();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);

        b.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                // connect 된 channel 의 pipeline 구성
                // inbound 는 위에서 아래로
                // outbound 는 아래서 위로 진행됨
                ch.pipeline().addLast(new CtDecoder() );
                ch.pipeline().addLast(new CtEncoder());
                ch.pipeline().addLast(new ClientHandler(connection));
            }
        });

        // connect 는 future 를 리턴함
        var cf = b.connect(addr, port);

        // future 에 addListener 를 사용하여
        // java future 로 변환
        cf.addListener((ChannelFuture future) -> {
            // lambda 에 ChannelFuture 타입을 지정하지 않으면  그냥 Future 타입이 되어버려
            // channel을 리턴받을 수 없으니 주의

            if (future.isSuccess()) {
                // 성공했을 때
                connection.channel = future.channel();
                ret.complete(connection);
            } else {
                // 실패했을 때
                System.out.println("connect failed");
                ret.completeExceptionally(future.cause());
            }
        });
        return ret;
    }

    public void connected(Channel channel) {
        client.connected(this);
    }

    public void disconnected(Channel channel) {
        client.disconnected(this);
    }

    public void close() {
        this.channel.close();
    }
}
