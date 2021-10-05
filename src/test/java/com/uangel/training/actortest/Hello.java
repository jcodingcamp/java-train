package com.uangel.training.actortest;

import java.util.concurrent.CompletableFuture;

public interface Hello {
    CompletableFuture<String> Hello();
    CompletableFuture<Stat> Stat();
    void Panic();
}
