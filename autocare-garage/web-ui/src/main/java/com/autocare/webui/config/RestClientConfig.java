package com.autocare.webui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * One RestClient per downstream service. Base URLs come from configuration so
 * the same image runs locally (localhost) and in Kubernetes (service DNS names).
 */
@Configuration
public class RestClientConfig {

    private static final ClientHttpRequestFactorySettings SETTINGS =
            ClientHttpRequestFactorySettings.DEFAULTS
                    .withConnectTimeout(Duration.ofSeconds(2))
                    .withReadTimeout(Duration.ofSeconds(4));

    @Bean
    public RestClient customersRestClient(@Value("${autocare.customers-service.url}") String baseUrl) {
        return build(baseUrl);
    }

    @Bean
    public RestClient workshopRestClient(@Value("${autocare.workshop-service.url}") String baseUrl) {
        return build(baseUrl);
    }

    private RestClient build(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(ClientHttpRequestFactories.get(SETTINGS))
                .build();
    }
}
