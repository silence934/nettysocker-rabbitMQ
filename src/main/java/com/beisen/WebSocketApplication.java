package com.beisen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author silence
 */
@Slf4j
@SpringBootApplication
public class WebSocketApplication {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SpringApplication.run(WebSocketApplication.class, args);
        log.info("启动时间: {}s", (System.currentTimeMillis() - start) / 1000.0);
    }

}
