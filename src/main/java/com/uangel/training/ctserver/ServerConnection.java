package com.uangel.training.ctserver;

import com.uangel.training.ctmessage.CtxMessage;
import io.netty.channel.Channel;

public class ServerConnection {
    Server server;
    Channel channel;

    public ServerConnection(Server server, Channel channel) {
        this.server = server;
        this.channel = channel;
    }

    public Server getServer() {
        return server;
    }

    public Channel getChannel() {
        return channel;
    }

    public void sendResponse(CtxMessage request, String msg) {
        // channel의 write 를 호출 하면
        // pipeline의 마지막 handler의 write 가 호출 됨
        channel.writeAndFlush(new CtxMessage(request.getTrid(), msg));
    }
}
