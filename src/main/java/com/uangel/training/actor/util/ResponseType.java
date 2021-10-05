package com.uangel.training.actor.util;

import akka.actor.ActorRef;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static akka.pattern.Patterns.ask;


// 프로토콜 코딩을 위한 interface
// actor의 request message 를 정의할 때 ResponseType 을 implements 하면서 , 원하는 Response type 을 명시
public interface ResponseType<T> {
    // type safe 하게 response 를 보낼 수 있는 helper 함수
    default void sendResponse(ActorRef pid , T response, ActorRef sender) {
        pid.tell(CompletableFuture.completedFuture(response), sender);
    }

    default void sendResponse(ActorRef pid , Throwable err, ActorRef sender) {
        pid.tell(CompletableFuture.failedFuture(err), sender);
    }

    // type safe 하게  future response 를 보낼 수 있는 helper 함수
    default void sendFutureResponse(ActorRef pid , CompletableFuture<T> future, ActorRef sender) {
        pid.tell(future, sender);
    }


    // type safe 하게 request 를 보낼 수 있는 helper함수
    static <T>CompletableFuture<T> askFor(ActorRef pid , ResponseType<T> request, Duration timeout) {
        return ask(pid , request, timeout).toCompletableFuture().thenCompose(x -> {
            return (CompletableFuture<T>)x;
        });
    }
}
