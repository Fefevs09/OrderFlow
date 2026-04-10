package com.order.service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class BeanConfig {

    @Bean
    public SnsClient snsClient() {
        return SnsClient.builder()
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .build();
    }
}
