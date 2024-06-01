package com.capstone.cschatbot.config.restcli;

import com.capstone.cschatbot.chat.domain.gpt.ChatResponse;
import com.capstone.cschatbot.chat.service.evaluation.EvaluationComponent;
import com.capstone.cschatbot.chat.service.gpt.GPTComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Value("${evaluation.url}")
    private String evaluationUrl;

    @Value("${openai.url}")
    private String gptUrl;

    @Value("${openai.secret-key}")
    private String gptSecret;

    @Bean
    public EvaluationComponent evaluationService() {
        RestClient restClient = RestClient.builder()
                .baseUrl(evaluationUrl)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(EvaluationComponent.class);
    }

    @Bean
    public GPTComponent gptComponent() {
        RestClient restClient = RestClient.builder()
                .baseUrl(gptUrl)
                .defaultHeader("Authorization", "Bearer " + gptSecret)
                .build();

        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(GPTComponent.class);
    }

}
