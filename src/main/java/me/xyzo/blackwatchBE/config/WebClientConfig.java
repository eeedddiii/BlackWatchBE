package me.xyzo.blackwatchBE.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${mongo.api.base-url:https://crawler.blackwatch.xyzo.me}")
    private String baseUrl;

    @Bean
    public WebClient mongoApiClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}