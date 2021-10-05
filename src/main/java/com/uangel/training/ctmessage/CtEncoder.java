package com.uangel.training.ctmessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CtEncoder extends MessageToByteEncoder<CtxMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CtxMessage msg, ByteBuf out) throws Exception {

        var b = msg.getMsg().getBytes();

        // header write
        out.writeLong(msg.getTrid());
        out.writeLong(b.length);

        // body write
        out.writeBytes(b);
    }
}
