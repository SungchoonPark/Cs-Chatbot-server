package com.capstone.cschatbot.chat.service.communicate;

import com.capstone.cschatbot.chat.entity.ChatRequest;
import com.capstone.cschatbot.chat.entity.ChatResponse;
import com.capstone.cschatbot.chat.entity.Evaluation;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
//@RequiredArgsConstructor
@Slf4j
public class CommunicateServiceImpl implements CommunicateService {
    @Value("${evaluation.url}")
    private String evaluationUrl;

    @Value("${openai.url}")
    private String apiUrl;
    private final RestTemplate evaluationRestTemplate;
    private final RestTemplate gptRestTemplate;

    public CommunicateServiceImpl(@Qualifier("evaluationRestTemplate") RestTemplate evaluationRestTemplate,
                                  @Qualifier("gptRestTemplate") RestTemplate gptRestTemplate) {
        this.evaluationRestTemplate = evaluationRestTemplate;
        this.gptRestTemplate = gptRestTemplate;
    }

    @Override
    public Evaluation withEvaluationServer(String clientAnswer) {
        URI uri = UriComponentsBuilder
                .fromUriString(evaluationUrl)
                .queryParam("answer", clientAnswer)
                .encode()
                .build()
                .toUri();

        Evaluation evaluation = evaluationRestTemplate.getForObject(uri, Evaluation.class);
        checkValidEvaluationResponse(evaluation);

        log.info("평가 내용 : {}", evaluation);
        return evaluation;
    }

    @Override
    public String withGPT(ChatRequest chatRequest) {
        ChatResponse gptResponse = gptRestTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
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
