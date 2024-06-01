package com.capstone.cschatbot.chat.service.evaluation;

import com.capstone.cschatbot.cs.domain.ChatEvaluation;
import com.capstone.cschatbot.chat.domain.evaluation.Evaluation;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService{
    private final EvaluationComponent evaluationComponent;

    @Override
    @Async
    public CompletableFuture<ChatEvaluation> getEvaluation(String question, String clientAnswer) {
        Evaluation evaluation = evaluationComponent.getEvaluation(question, clientAnswer);
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
