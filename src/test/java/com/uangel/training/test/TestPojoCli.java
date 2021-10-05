package com.uangel.training.test;


import com.uangel.training.ctclient.ClientFactory;
import com.uangel.training.ctserver.Server;
import com.uangel.training.modules.PojoClientModule;
import com.uangel.training.modules.ServerModule;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;


public class TestPojoCli {

    @Test
    public void test() throws ExecutionException, InterruptedException {

        var application = new AnnotationConfigApplicationContext(PojoClientModule.class, ServerModule.class);
        try {

            var server = (CompletableFuture<Server>) application.getBean("server8080");
            //var server = BeanFactoryAnnotationUtils.qualifiedBeanOfType(application.getBeanFactory(), ResolvableType.forClassWithGenerics(CompletableFuture.class, Server.class), "server-8080");

            var cf = application.getBean(ClientFactory.class);


            var client = server.thenCompose(s -> {
                return cf.New("127.0.0.1", 8080, 2);
            });


            var response = client.thenCompose(client1 -> {
                return client1.sendRequest("world");
            });

            try {
                assertEquals("hello world", response.get());
            } finally {
                //client.thenAccept(x -> x.close());
                server.thenAccept(x -> x.close());
            }
        } finally {
            application.close();
        }


    }
}
