package com.uangel.training.actortest;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.uangel.training.actor.util.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static akka.pattern.Patterns.ask;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Component
public class HelloActorInterface implements Hello {

    ActorRef pid;
    @Autowired
    public HelloActorInterface(Config config ,  ActorSystem actorSystem) {
       pid = actorSystem.actorOf(HelloActor.props(config));
    }

    private <T> CompletableFuture<T> askFor(ActorRef pid , ResponseType<T> request, Duration timeout) {
        return ask(pid , request, timeout).toCompletableFuture().thenCompose(x -> {
            return (CompletableFuture<T>)x;
        });
    }

    @Override
    public CompletableFuture<String> Hello() {
        return askFor(pid, new messageRequest(), Duration.ofSeconds(5));
    }

    @Override
    public CompletableFuture<Stat> Stat() {
        return askFor(pid, new messageStatRequest(), Duration.ofSeconds(5));
    }

    @Override
    public void Panic() {
        pid.tell(new messagePanic() , ActorRef.noSender());
    }
}
