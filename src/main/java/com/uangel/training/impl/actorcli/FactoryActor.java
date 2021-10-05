package com.uangel.training.impl.actorcli;

import akka.actor.AbstractActor;
import akka.actor.Props;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

class FactoryActor extends AbstractActor  {

    EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Override
    public void postStop()  {
        workerGroup.shutdownGracefully();
    }

    public static Props props() {
        return Props.create(FactoryActor.class, () -> new FactoryActor());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(messageNewClient.class, this::onNewClient)
                .build();
    }

    // messageNewClient 를 받으면 실행됨
    private void onNewClient(messageNewClient req) {

        // new 를 받을 때마다 child 생성
        var child = this.getContext().actorOf( childActor.props(req, workerGroup));

        req.sendResponse(sender(), new ClientActorInterface(child), self());

    }


}
