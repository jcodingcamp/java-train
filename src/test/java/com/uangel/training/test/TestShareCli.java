package com.uangel.training.test;

import com.uangel.training.ctclient.ClientFactory;
import com.uangel.training.ctserver.Server;
import com.uangel.training.ctserver.ServerFactory;
import com.uangel.training.modules.ActorModule;
import com.uangel.training.modules.ServerModule;
import com.uangel.training.modules.ShareClientModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ShareClientModule.class,  ActorModule.class, ServerModule.class})
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class TestShareCli {

    @Autowired
    ClientFactory cf;

    @Autowired
    @Qualifier("server-8080")
    CompletableFuture<Server> server;

    @Test
    public void test() throws ExecutionException, InterruptedException {

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


    }

    @Test
    public void testLazy() throws ExecutionException, InterruptedException {

    }

    @Test
    public void testSharedConnection() throws ExecutionException, InterruptedException {


        var client = server.thenCompose(s -> {
            return cf.New("127.0.0.1", 8080, 2);
        });


        var response = client.thenCompose(client1 -> {
            return client1.sendRequest("world");
        });

        assertEquals("hello world", response.get());

        client = server.thenCompose(s -> {
            return cf.New("127.0.0.1", 8080, 2);
        });

        Thread.sleep(12000);


        response = client.thenCompose(client1 -> {
            return client1.sendRequest("world");
        });

        assertEquals("hello world", response.get());

    }

    @Autowired
    ServerFactory sf;

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws Throwable {

        var server = sf.newServer(8081, (con,request) -> {
            //request.getServer().sendResponse(request, "hello " + request.getMessage().getMsg());
        });

        var client = server.thenCompose(s -> {
            return cf.New("127.0.0.1", 8081, 2);
        });


        var response = client.thenCompose(client1 -> {
            return client1.sendRequest("world");
        });

        try {
            assertEquals("hello world", response.get());
        } catch (ExecutionException e) {
            throw e.getCause();
        } finally {
            client.thenAccept(x -> x.close());
            server.thenAccept(x -> x.close());
        }

    }


    @Test
    public void testNoServer() throws Throwable {


        var client = cf.New("127.0.0.1", 8081, 2);


        Thread.sleep(3000);

        var si = new ServerFactory();
        try {
            var server = si.newServer(8081, (con, request) -> {
                con.sendResponse(request, "hello " + request.getMsg());
            });

            Thread.sleep(3000);

            var response = client.thenCompose(client1 -> {
                return client1.sendRequest("world");
            });

            try {
                assertEquals("hello world", response.get());
            } catch (ExecutionException e) {
                throw e.getCause();
            } finally {
                client.thenAccept(x -> x.close());
                server.thenAccept(x -> x.close());
            }

        } finally {
            si.close();
        }

    }
}
