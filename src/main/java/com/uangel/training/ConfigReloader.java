package com.uangel.training;

import com.typesafe.config.Config;

// config 가 바뀌었을 때,  reload 이벤트를 받기 위한 인터페이스
// ConfigMonitorModule 을 참조
public interface ConfigReloader {
    void Reload(Config cfg);
}
