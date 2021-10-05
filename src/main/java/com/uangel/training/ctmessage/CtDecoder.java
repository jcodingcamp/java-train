package com.uangel.training.ctmessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class CtDecoder extends ByteToMessageDecoder {
    @Override

    // ByteToMessageDecoder 의 decode 를 구현하면  ByteBuf 의 release 를 호출할 필요 없음.
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {

        // 충분한 크기만큼 read 되지 않았을 때,  원래대로 돌리기 위해 index 저장
        var index = byteBuf.readerIndex();


        // header의 size가 16 byte 여서
        // header가 완전히 read 되었는지 테스트
       if (byteBuf.readableBytes() >= 16) {

           // readLong 등의 함수를 호출하면  index 가 증가됨
           var trid = byteBuf.readLong();
           var length = byteBuf.readLong();

           // body 의 size 만큼 read 되었는지 테스트
           if (byteBuf.readableBytes() >= length) {
               byte[] b = new byte[(int)length];

               byteBuf.readBytes(b);

               // 메시지를 decoding 했으면  output list 에 추가
               list.add(new CtxMessage(trid , new String(b)));
               return;
           }
       }

       // 충분한 길이만큼 read가 안되었으면  , index 를 원래대로 돌림
        byteBuf.readerIndex(index);
    }
}
