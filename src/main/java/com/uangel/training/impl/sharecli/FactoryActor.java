package com.uangel.training.impl.sharecli;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
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
                .match(messageSendRequest.class, this::onSendRequest)
                .build();
    }



    ActorRef getChild(messageNewClient spec) {
        var childName = String.format("%s-%d", spec.addr, spec.port);

        var childOpt = this.getContext().findChild(childName);

        return childOpt.orElseGet(() -> {
            return this.context().actorOf(childActor.props(spec, workerGroup), childName);
        });
    }

    private void onNewClient(messageNewClient req) {

        getChild(req);

        // actorcli 와 비교했을 때 달라지는 점은 , ClientActorInterface 를 생성할 때
        // actor ref 를 child actor가 아니라 FactoryActor 를 지정했다는 점
        // 그래서 messageSendRequest 가 child actor로 바로 가지 않고
        // 항상 FactoryActor 를 거쳐가게 된다
        req.sendResponse(sender(), new ClientActorInterface(self(), req), self());

    }

    // messageSendRequest 를 받았을 때
    void onSendRequest( messageSendRequest r ) {

        // getChild 를 하게 되면 , child 가 없어진 경우 다시만들게 된다
        getChild(r.spec).forward(r, context());
    }

}
