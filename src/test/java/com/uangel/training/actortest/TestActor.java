package com.uangel.training.actortest;

import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;

public class TestActor {
    @Test
    public void test() throws InterruptedException {
        var actorsystem = ActorSystem.create("mysystem");

        var hello = new HelloActorInterface(ConfigFactory.empty(), actorsystem);

        hello.Hello().thenAccept(s -> {
            System.out.println(s);
        });

        hello.Hello().whenComplete((s, throwable) -> {
            if(throwable!=null) {
                System.out.printf("%s\n", throwable.toString());
            }
        });

        hello.Stat().thenAccept(s -> {
            System.out.printf("numreq = %d , last time = %s\n", s.NumReq, s.LastEvent);
        });

        hello.Panic();

        hello.Stat().thenAccept(s -> {
            System.out.printf("numreq = %d , last time = %s\n", s.NumReq, s.LastEvent);
        });

        Thread.sleep(1000);
    }
}
