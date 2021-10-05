package com.uangel.training.impl.actorcli;

import akka.actor.ActorRef;
import com.uangel.training.actor.util.ResponseType;
import com.uangel.training.ctclient.Client;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class messageClose {

}

class messageSendRequest implements ResponseType<String> {

    String msg;

    public messageSendRequest(String msg) {
        this.msg = msg;
    }
}


// Client 의 구현
// 여기서는 method -> message 로 변환하는 작업만 수행
public class ClientActorInterface implements Client {

    private ActorRef ref;

    public ClientActorInterface(ActorRef child) {
        this.ref = child;
    }

    @Override
    public void close() {
        ref.tell(new messageClose(), ActorRef.noSender());
    }

    @Override
    public CompletableFuture<String> sendRequest(String msg) {
        return ResponseType.askFor(ref, new messageSendRequest(msg), Duration.ofSeconds(5));
    }
}
