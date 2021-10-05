package com.uangel.training.impl.sharecli;

import akka.actor.ActorRef;
import com.uangel.training.actor.util.ResponseType;
import com.uangel.training.ctclient.Client;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class messageClose {

}

class messageSendRequest implements ResponseType<String> {

    String msg;
    messageNewClient spec;

    public messageSendRequest(String msg, messageNewClient spec) {
        this.msg = msg;
        this.spec = spec;
    }
}

public class ClientActorInterface implements Client {

    private ActorRef ref;
    private messageNewClient spec;

    public ClientActorInterface(ActorRef factoryRef , messageNewClient spec) {

        this.ref = factoryRef;
        this.spec = spec;
    }

    @Override
    public void close() {
        // actorcli 와 비교했을 때 달라지는 점은
        // close 가 호출되어도 아무것도 하지 않는다는 점
        // actor 는 일정시간 동안 request 가 없으면 , 자동으로 종료된다.
    }

    @Override
    public CompletableFuture<String> sendRequest(String msg) {
        return ResponseType.askFor(ref, new messageSendRequest(msg, spec), Duration.ofSeconds(5));
    }
}
