package com.uangel.training.modules;

import com.uangel.training.hello.Hello;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Import({ActorModule.class, ActorClientModule.class})
// ActorSystem 과  ClientFactory 를 제공합니다.
// 필요한 모듈은 없습니다.
public class DefaultModule {
    @Bean
    @Order(0)
    public Hello hello() {
        return new Hello() {
            @Override
            public String say() {
                return "hello world";
            }
        };
    }
}
