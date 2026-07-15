package com.banknifty;

import com.banknifty.config.KiteProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableRetry
@EnableConfigurationProperties({
        KiteProperties.class
})
public class BankNiftyOptionAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankNiftyOptionAnalyzerApplication.class, args);
    }

}