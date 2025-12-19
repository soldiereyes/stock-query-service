package com.techsolution.stockquery.infrastructure.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class FeignLoggingConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

