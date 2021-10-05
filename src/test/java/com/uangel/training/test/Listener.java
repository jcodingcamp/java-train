package com.uangel.training.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Listener {

    interface WebServerEventListener {
        void onStart( WebServer server, int port);
        void onError(WebServer server , Throwable ex);
        void onTerminate(WebServer server);
    }

    abstract class WebServerEventListenerAdaptor implements WebServerEventListener {
        @Override
        public void onStart(WebServer server, int port) {

        }

        @Override
        public void onError(WebServer server, Throwable ex) {

        }

        @Override
        public void onTerminate(WebServer server) {

        }
    }

    interface RequestListener {
        void onRequest(WebServer server, Object request);
        void onPost(WebServer server, Object request);
        void onGet(WebServer server, Object request);

    }

    abstract class RequestListenerAdaptor implements RequestListener {
        @Override
        public void onRequest(WebServer server, Object request) {

        }
    }

    class WebServer {

        List<WebServerEventListener> listeners = new ArrayList<>();

         public void addListener( WebServerEventListener listener) {
            listeners.add(listener);
        }

        public void addRequestListener( RequestListener listener) {
        }

        public void close() {
             listeners.stream().forEach(l -> l.onTerminate(this));

             var itr = listeners.iterator();
             while(itr.hasNext()) {
                 var next = itr.next();
                 next.onTerminate(this);
             }

             for(int i=0;i<listeners.size();i++) {
                 listeners.get(i).onTerminate(this);
             }
        }
    }

    class MyApp extends WebServerEventListenerAdaptor implements RequestListener{
        WebServer server;

        public MyApp() {
            this.server = new WebServer();
            this.server.addListener(this);
            this.server.addRequestListener(this);
        }

        @Override
        public void onTerminate(WebServer server) {

        }

        @Override
        public void onRequest(WebServer server, Object request) {

        }

        @Override
        public void onPost(WebServer server, Object request) {

        }

        @Override
        public void onGet(WebServer server, Object request) {

        }
    }

    @Test
    public void test() {
        var a = new WebServer();
        a.addListener(new WebServerEventListenerAdaptor() {
            @Override
            public void onTerminate(WebServer server) {

            }
        });

    }
}
