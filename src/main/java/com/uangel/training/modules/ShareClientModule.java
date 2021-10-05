package com.uangel.training.modules;

import com.uangel.training.impl.sharecli.FactoryActorInterface;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FactoryActorInterface.class})
// ClientFactory 를 제공합니다.
// ActorSystem 을 필요로 합니다. ActorModule 과 같이 사용가능합니다.
public class ShareClientModule {
}
