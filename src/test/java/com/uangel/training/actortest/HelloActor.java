package com.uangel.training.actortest;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.typesafe.config.Config;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

public class HelloActor extends AbstractActor {

    private Config config;
    private actorData data;
    private OffsetDateTime lastEventTime;

    public HelloActor(Config config, actorData data) {
        this.config = config;
        this.data = data;
    }

    static class actorData {
        int numReq;
    }



    public static Props props(Config config) {
        var data = new actorData();
        return Props.create(HelloActor.class, () -> new HelloActor(config, data));
    }

    private void onHello( messageRequest r ) {
        System.out.printf("receive message %s\n", r);
        if (data.numReq == 1) {
            r.sendResponse(sender() , new NoSuchElementException("not found"), self());
        } else {
            r.sendResponse(sender(), "hello world", self());
        }
        // sender().tell(11, self());
        data.numReq++;
        lastEventTime = OffsetDateTime.now();
    }

    private void onStat( messageStatRequest r) {
        r.sendResponse(sender(), new Stat(data.numReq, lastEventTime), self());
    }

    private void onPanic(messagePanic r) {
        throw new RuntimeException("panic");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(messageRequest.class, this::onHello)
            .match(messageStatRequest.class , this::onStat)
            .match(messagePanic.class, this::onPanic)
        .build();
    }
}
