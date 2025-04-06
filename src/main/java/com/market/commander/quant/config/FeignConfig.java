package com.market.commander.quant.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(
                2400000,
                2400000
        );
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

}
