package com.uangel.training.ctserver;

import com.uangel.training.ctmessage.CtxMessage;
import io.netty.channel.*;

import java.util.function.BiConsumer;

public class ServerHandler extends ChannelDuplexHandler {

    Server server;
    private BiConsumer<ServerConnection,CtxMessage> consumer;

    public ServerHandler(Server server, BiConsumer<ServerConnection, CtxMessage> consumer) {
        this.server = server;
        this.consumer = consumer;
    }

    @Override
    // accept 되었을 때
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("connected");
        server.channelConnected(ctx.channel());
    }

    @Override
    // 끊어 졌을 때
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("disconnected");
        server.channelDisconnected(ctx.channel());

    }

    @Override
    // message 를 read 했을 때
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CtxMessage) {
            var request = (CtxMessage)msg;
            System.out.println("message read = " + request.getMsg());
            consumer.accept(new ServerConnection(server , ctx.channel()),  request);
        }
    }

    @Override
    // channel의 write가 호출 되었 을 때
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof CtxMessage) {
            var response = (CtxMessage)msg;
            System.out.println("message = " + response.getMsg());

            // ctx 를 사용하여  pipeline의 다음 단계로 전파
            ctx.write(msg, promise);
        }
        //super.write(ctx, msg, promise);
    }
}
