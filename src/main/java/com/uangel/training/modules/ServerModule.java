package com.uangel.training.modules;

import com.uangel.training.ctserver.Server;
import com.uangel.training.ctserver.ServerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.CompletableFuture;

@Configuration
@Lazy
// ServerFactory 와  8080 port 에 binding 된 서버를 제공합니다.
// 필요한 모듈은 없습니다.
public class ServerModule {
    @Bean
    public ServerFactory serverThreadPool() {
        return new ServerFactory();
    }

    @Bean
    @Qualifier("server-8080")
    CompletableFuture<Server> server8080(ServerFactory pool) {
        return pool.newServer(8080, (con,request) -> {
            con.sendResponse(request, "hello " + request.getMsg());
        });
    }

    // 위에서 binding 된 CompletableFuture<Server> 는 close 함수가 없기 때문에
    // close 해주는 bean 을 등록합니다.
    @Bean
    public AutoCloseable serverCloser( @Qualifier("server-8080")CompletableFuture<Server> server) {

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                server.thenAccept(s -> s.close());
            }
        };

    }

}
