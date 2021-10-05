package com.uangel.training.modules;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;

@Configuration
@Lazy
// Config 와 ActorSystem 을 제공합니다.
// 필요한 모듈은 없습니다
public class ActorModule {


    @Bean
    public Config config() {
//        var cfg =  ConfigFactory.parseFile(new File("/Users/gura/git/ulib/test/testActor/reference.conf"));
//
//        if (cfg.hasPath("xx.xx")) {
//            cfg.getString("xx.xx");
//        }
//        System.out.println("cfg = " +  cfg.root().render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false)));
//
//        return ConfigFactory.load(cfg);

        return ConfigFactory.load();
    }

    // ActorSystem 은 Close 함수가 없어서
    // destroyMethod 로 close 할 때 호출될 메소드를 지정해야 합니다.
    @Bean(destroyMethod = "terminate")
    public ActorSystem actorSystem(  Config cfg ) {
        return ActorSystem.create("mysystem", cfg);
    }
}
