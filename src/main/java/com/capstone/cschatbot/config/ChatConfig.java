package com.capstone.cschatbot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatConfig {
    @Value("${openai.secret-key}")
    private String secretKey;

    @Bean
    @Qualifier("chatRestTemplate")
    public RestTemplate chatRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + secretKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
