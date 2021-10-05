package com.uangel.training.test;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class TestPromise {

    @Test
    public void test() throws InterruptedException {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "hello";
        });

        CompletableFuture<String> f2 = new CompletableFuture<>();


        f2.thenAccept(s -> {
            System.out.println(s);
        });


        Thread.sleep(30);
        System.out.println("before complete");


        f2.completeExceptionally(new IOException("too bad"));
        f2.complete("world");
        f2.complete("hello");

        f2.thenAccept(s -> {
            System.out.println(s);
        });

        f2.whenComplete((s, throwable) -> {
            if (throwable != null ) {
                throwable.printStackTrace();
            }
        });
    }

    public CompletableFuture<Integer> otherApi() {
        return CompletableFuture.completedFuture(10);
    }
    public CompletableFuture<String> api() {

        var ret = new CompletableFuture<String>();


        otherApi().whenComplete((integer, throwable) -> {
            if (throwable != null) {
                ret.completeExceptionally(throwable);
            } else {
                ret.complete(String.format("%d", integer));
            }
        });

        return ret;


    }

    @Test
    public void test2() {
        var f = api();


        f.thenAccept(s -> System.out.println(s));
    }
}
