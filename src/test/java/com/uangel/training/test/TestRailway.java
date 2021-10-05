package com.uangel.training.test;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class TestRailway {

    Optional<URI> parseURI( String s )   {
        try {
            return Optional.of(new URI(s));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
    @Test
    public void test() {
        var optURI = Optional.of(":::+-*$&");

        var optParsed = optURI.flatMap(this::parseURI);

        optParsed.ifPresent( (uri) -> {
            System.out.println("send request to  " + uri);
        });


    }


    class NrfClient {
        public NrfClient(String nrfID) {
        }

        CompletableFuture<String> getToken( String targetNfType ) {
            return CompletableFuture.completedFuture("token");
        }
    }

    class NfClient {
        private String nfID;

        public NfClient(String nfID) {
            this.nfID = nfID;
        }

        CompletableFuture<String> sendRequest(String token, String method , String path , String body) {
            if(this.nfID == "inactive") {
                return CompletableFuture.failedFuture(new TimeoutException("timeout"));
            }
            return CompletableFuture.completedFuture("response from " + nfID);
        }
    }

    @Test
    public void testRetry() throws ExecutionException, InterruptedException {
        var nrfcli = new NrfClient("nrf");
        var inactiveCli = new NfClient("inactive");
        var activeCli = new NfClient("active");
        var token = nrfcli.getToken("amf");
        var res = token.thenCompose(t -> inactiveCli.sendRequest(t, "GET", "/api/v1", "hello"))
                .handle((s, throwable) -> {
                   if (throwable != null) {
                       return token.thenCompose(t -> activeCli.sendRequest(t, "GET", "api/v1", "hello"));
                   } else {
                       return CompletableFuture.completedFuture(s);
                   }
                }).thenCompose(x -> x);

        assertEquals("response from active", res.get());
    }
}
