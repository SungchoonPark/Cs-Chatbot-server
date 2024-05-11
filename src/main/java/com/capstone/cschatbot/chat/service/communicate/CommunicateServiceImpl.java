package com.capstone.cschatbot.chat.service.communicate;

import com.capstone.cschatbot.chat.entity.ChatRequest;
import com.capstone.cschatbot.chat.entity.ChatResponse;
import com.capstone.cschatbot.chat.entity.Evaluation;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunicateServiceImpl implements CommunicateService{
    @Value("${evaluation.url}")
    private String evaluationUrl;

    @Value("${openai.url}")
    private String apiUrl;

    @Qualifier("chatRestTemplate")
    @Autowired
    private final RestTemplate restTemplate;

    @Override
    public Evaluation withEvaluationServer(String clientAnswer) {
        URI uri = UriComponentsBuilder
                .fromUriString(evaluationUrl)
                .queryParam("q", clientAnswer)
                .encode()
                .build()
                .toUri();

        // TODO : RestTemplate 객체의 빈번한 생성을 막아야함
        RestTemplate evaluationRestTemplate = new RestTemplate();
        Evaluation evaluation = evaluationRestTemplate.getForObject(uri, Evaluation.class);
        checkValidEvaluationResponse(evaluation);

        log.info("평가 내용 : {}", evaluation);
        return evaluation;
    }

    @Override
    public String withGPT(ChatRequest chatRequest) {
        ChatResponse gptResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        checkValidGptResponse(gptResponse);

        return gptResponse.getChoices().get(0).getMessage().getContent();
    }

    private void checkValidGptResponse(ChatResponse gptResponse) {
        if (gptResponse == null || gptResponse.getChoices() == null || gptResponse.getChoices().isEmpty()) {
            throw new CustomException(CustomResponseStatus.GPT_NOT_ANSWER);
        }
    }

    private void checkValidEvaluationResponse(Evaluation evaluation) {
        if (evaluation == null) {
            throw new CustomException(CustomResponseStatus.EVALUATION_SERVER_NOT_ANSWER);
        }
    }
}
