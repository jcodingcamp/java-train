package com.uangel.training.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class TestLogging {
    Logger cdrLogger = LoggerFactory.getLogger("CDR");


    @Test
    public void test() {
        log.info("hello world");
        log.error("error");
        cdrLogger.info("cdr");
    }

}
