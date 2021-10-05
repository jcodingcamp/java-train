package com.uangel.training.test;

import com.uangel.training.ctclient.ClientFactory;
import com.uangel.training.modules.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ShareClientModule.class, ActorModule.class , ConfigMonitorModule.class})
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class TestConfigReload {


    @Autowired
    ClientFactory cf;

    @Test
    public void test() throws InterruptedException {
        // config reload 출력이 4~5 번 나와야 합니다.
        Thread.sleep(5000);
    }
}
