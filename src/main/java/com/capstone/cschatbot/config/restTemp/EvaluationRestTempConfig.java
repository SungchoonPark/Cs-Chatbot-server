package com.capstone.cschatbot.config.restTemp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EvaluationRestTempConfig {

    @Bean
    @Qualifier("evaluationRestTemplate")
    public RestTemplate evaluationRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> execution.execute(request, body));
        return restTemplate;
    }
}
