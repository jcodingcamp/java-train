package com.uangel.training.modules;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.uangel.training.ConfigReloader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
public class ConfigMonitorModule  {

    // injector 를 inject 받습니다.
    @Autowired
    ApplicationContext applicationContext;

    @Bean
    public AutoCloseable configMonitor() throws IOException {

        System.out.println("start config monitor");
        var stopFlag = new AtomicBoolean(true);

        // 다음과 같이 WatchService 를 이용하여 file watch 를 하고
        // 변경되면 load 해야 하는데
        var watchService = FileSystems.getDefault().newWatchService();

        // 귀찮으니까 1초에 한번씩 reload 이벤트를 강제로 보내는 thread 를 만듭니다.
        var t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(stopFlag.get()) {
                    try {
                        Thread.sleep(1000);

                        // 파일이 변경되면 , config 를 새로 load 합니다.
                        //var newConfig = ConfigFactory.parseFile(new File("some-path"));

                        // 파일이 없으니까 empty 를 대신 사용
                        var newConfig = ConfigFactory.empty();

                        // load 를 사용해서  환경 변수등을 치환합니다.
                        var resolved = ConfigFactory.load(newConfig);

                        // 그 후에 injector 에 있는 모든 ConfigReloader 타입에 대해서
                        // Reload 를 호출 해 줍니다.
                        var beans = applicationContext.getBeansOfType(ConfigReloader.class);
                        beans.forEach((s, configReloader) -> {
                            configReloader.Reload(resolved);
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();


        // watch service 를 close 하는 것을 리턴합니다.
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                watchService.close();
                stopFlag.set(false);
            }
        };
    }

}
