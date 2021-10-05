package com.uangel.training.ctserver;

import com.uangel.training.ctmessage.CtDecoder;
import com.uangel.training.ctmessage.CtEncoder;
import com.uangel.training.ctmessage.CtxMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ServerFactory implements AutoCloseable {

    // channel accept 가 실행될 thread pool
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    // accept 된  channel 의  handler 가 실행될 thread pool
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int port;


    public CompletableFuture<Server> newServer(int port, BiConsumer<ServerConnection, CtxMessage> consumer) {
        this.port = port;

        var server = new Server();
        CompletableFuture<Server> ret = new CompletableFuture<>();


        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.channel(NioServerSocketChannel.class);

        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {

                //accept 된 channel의 pipeline 구성
                // inbound 는 위에서 아래로
                // outbound 는 아래서 위로 진행됨
                socketChannel.pipeline().addLast(new CtDecoder() );
                socketChannel.pipeline().addLast(new CtEncoder());
                socketChannel.pipeline().addLast(new ServerHandler(server, consumer));

            }
        });


        System.out.println("bind port " + port);

        // bind 는 future 를 리턴함
        // .sync 를 사용하 blocking 할 수 있긴 하지만
        var cf = b.bind(port);

        // future 에 addListener 를 사용하여,
        // java future 로 변환
        cf.addListener(( ChannelFuture future) -> {
            // lambda 에 ChannelFuture 타입을 지정하지 않으면  그냥 Future 타입이 되어버려
            // channel을 리턴받을 수 없으니 주의

            if (future.isSuccess()) {
                // 성공했을 때 channel() 로 연결된 channel을 받을 수 있음
                System.out.println("bind success : " + port);
                server.Bind(future.channel());
                ret.complete(server);
            } else {
                // 실패했을 때 cause 에 원인이 있음
                System.out.println("bind failed : " + port);
                future.cause().printStackTrace();
                ret.completeExceptionally(future.cause());
            }
        });


        return ret;
    }

    public void close() {
        System.out.println("close port " + port);
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
