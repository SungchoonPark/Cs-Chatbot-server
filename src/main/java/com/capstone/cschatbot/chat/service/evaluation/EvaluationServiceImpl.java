package com.capstone.cschatbot.chat.service.evaluation;

import com.capstone.cschatbot.cs.domain.ChatEvaluation;
import com.capstone.cschatbot.chat.domain.evaluation.Evaluation;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService{

    @Value("${evaluation.url}")
    private String evaluationUrl;

    @Qualifier("evaluationRestTemplate")
    @Autowired
    private final RestTemplate evaluationRestTemplate;

    @Override
    @Async
    public CompletableFuture<ChatEvaluation> getEvaluation(String question, String clientAnswer) {
        URI uri = UriComponentsBuilder
                .fromUriString(evaluationUrl)
                .queryParam("question", question)
                .queryParam("answer", clientAnswer)
                .encode()
                .build()
                .toUri();

        Evaluation evaluation = evaluationRestTemplate.getForObject(uri, Evaluation.class);
        checkValidEvaluationResponse(evaluation);
        log.info("평가 : {}", evaluation.getEvaluation());

        return CompletableFuture.completedFuture(ChatEvaluation.of(question, clientAnswer, evaluation.getEvaluation()));
    }

    private void checkValidEvaluationResponse(Evaluation evaluation) {
        if (evaluation == null) {
            throw new CustomException(CustomResponseStatus.EVALUATION_SERVER_NOT_ANSWER);
        }
    }
}
