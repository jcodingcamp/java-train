package com.uangel.training.ctclient;

import java.util.concurrent.CompletableFuture;


// Client 를 생성하는 Factory
// 구현은 pojocli , actorcli, sharecli 세종류가 있음
public interface ClientFactory {
    CompletableFuture<Client> New(String addr , int port , int numConnection);
}
