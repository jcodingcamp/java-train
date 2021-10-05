package com.uangel.training.ctclient;

import java.util.concurrent.CompletableFuture;

public class Request {
    String msg;
    CompletableFuture<String> promise;

    public Request(String msg, CompletableFuture<String> promise) {
        this.msg = msg;
        this.promise = promise;
    }
}
