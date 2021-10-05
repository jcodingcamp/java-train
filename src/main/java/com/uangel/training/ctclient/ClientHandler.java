package com.uangel.training.ctclient;

import com.uangel.training.ctmessage.CtxMessage;
import io.netty.channel.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientHandler extends ChannelDuplexHandler {

    // ongoing transaction map
    // eventLoop 안에서 실행되기 때문에 lock 필요 없음
    Map<Long, Request> transactions = new HashMap<>();

    // transaction id 생성을 위한 id
    // eventLoop 안에서 실행되기 때문에 lock 필요 없음
    long nextID = 0;

    private ClientConnection clientConnection;

    public ClientHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        clientConnection.connected(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientConnection.disconnected(ctx.channel());
    }



    // request 를 write 하는 부분
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Request) {
            var request = (Request)msg;

            //transaction id 생성
            var trid = nextID++;

            // 여기서 promise 가 두 종류 있는데
            // 아규먼트의 ChannelPromise 는  write 성공 여부에 대한 promise 이고
            // request 안의 promise 는 response 가 왔는지에 대한 promise 임

            // 다음 pipeline 에 write
            // 이때  write 아규먼트로 받은 ChannelPromise 가 같이 전달 되어야 함
            // write의 결과는 future
             var wf = ctx.write(new CtxMessage( trid, request.msg), promise);

             // transaction map 에 추가
             transactions.put(trid, request);

             // write 가 성공하면 아무것도 하지 않고 response 기다림
            // write 가 실패했을 때는 response가 오지 않기 때문에  request의 promise 로 fail 전송
             wf.addListener((ChannelFuture future) -> {
                 System.out.println("write future is in event loop? = " + ctx.executor().inEventLoop());

                if (!future.isSuccess()) {
                    // transaction map 에서 삭제
                    transactions.remove(trid);

                    // promise 에 error 설정
                    request.promise.completeExceptionally(future.cause());
                } else {
                    // write 에 성공했는데 , timeout 났을 때 , timeout 시간은 5초
                    ctx.executor().schedule(() -> {
                        // transaction map 에서 삭제
                        transactions.remove(trid);

                        // request의 promise로 timeout 에러 전송
                        request.promise.completeExceptionally(new TimeoutException("time out"));
                    }, 5 , TimeUnit.SECONDS);
                }
             });
        }
    }


    // response 를 read 했을 때
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof CtxMessage) {
            var response = (CtxMessage) msg;
            // transaction map 에서 transaction id 로 request 를 찾음
            var request = transactions.remove(response.getTrid());
            if (request != null ) {
                // transaction 이 timeout 되지 않고 존재하면
                // promise 에  성공 전송
                request.promise.complete(response.getMsg());
            }
        }
    }

}
