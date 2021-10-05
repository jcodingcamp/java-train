package com.uangel.training.test;

import com.uangel.training.hello.Hello;
import com.uangel.training.modules.DefaultModule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Priority;


@Lazy
@Configuration
class MockupHelloSameName {

    @Bean
    public Hello hello() {
        return new Hello() {
            @Override
            public String say() {
                return "mockup world";
            }
        };
    }
}

@Configuration
@Lazy
class MockupHelloNotSameName {

    @Bean
    @Primary
    public Hello mockuphello() {
        return new Hello() {
            @Override
            public String say() {
                return "mockup not same";
            }
        };
    }
}

@Priority(10)
class MockupHello implements Hello {
    @Override
    public String say() {
        return "mockup order 10";
    }
}

@Configuration
@Lazy
@Import(MockupHello.class)
class MockupHelloHasOrder {

}



@RunWith(SpringRunner.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
@ContextConfiguration(classes = {DefaultModule.class, MockupHelloSameName.class})
public class TestModuleOverride {

    @Autowired
    Hello h;

    @Test
    public void test() {
        Assert.assertEquals("mockup world", h.say());
    }

    @Test
    public void testNotSameName( ) {
        try(var application = new AnnotationConfigApplicationContext(DefaultModule.class, MockupHelloNotSameName.class)) {
            var hello = application.getBean(Hello.class);
            Assert.assertEquals("mockup not same", hello.say());
        }
    }

    @Test
    public void testHasOrder( ) {
        try(var application = new AnnotationConfigApplicationContext(DefaultModule.class, MockupHelloHasOrder.class)) {
            var hello = application.getBean(Hello.class);
            Assert.assertEquals("mockup order 10", hello.say());
        }
    }



}
