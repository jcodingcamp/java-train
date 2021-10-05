package com.uangel.training.impl.sharecli;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.uangel.training.ConfigReloader;
import com.uangel.training.actor.util.ResponseType;
import com.uangel.training.ctclient.Client;
import com.uangel.training.ctclient.ClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class messageNewClient implements ResponseType<Client> {

    String addr;
    int port;
    int numConnection;

    public messageNewClient(String addr, int port, int numConnection) {
        this.addr = addr;
        this.port = port;
        this.numConnection = numConnection;
    }
}

@Lazy
public class FactoryActorInterface implements ClientFactory, AutoCloseable , ConfigReloader {

    ActorRef mainActorRef;
    private ActorSystem actorSystem;
    private Config cfg;

    @Autowired
    public FactoryActorInterface(ActorSystem actorSystem, Config cfg) {
        this.cfg = cfg;

        System.out.println("new factory actor interface");
        mainActorRef = actorSystem.actorOf(FactoryActor.props(), "client-factory");

        this.actorSystem = actorSystem;
    }

    @Override
    public CompletableFuture<Client> New(String addr, int port, int numConnection) {
        return ResponseType.askFor(mainActorRef, new messageNewClient(addr , port , numConnection), Duration.ofSeconds(5));
    }

    @Override
    public void close() throws Exception {
        actorSystem.stop(mainActorRef);
    }

    @Override
    public void Reload(Config cfg) {
        this.cfg = cfg;
        System.out.println("config reload");
    }
}
