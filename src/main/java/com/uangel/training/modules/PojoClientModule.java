package com.uangel.training.modules;

import com.uangel.training.impl.pojocli.FactoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FactoryImpl.class)
// ClientFactory 를 제공합니다.
// 필요한 모듈은 없습니다.
public class PojoClientModule {
}
