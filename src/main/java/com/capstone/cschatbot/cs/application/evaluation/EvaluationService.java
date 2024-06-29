package com.capstone.cschatbot.cs.application.evaluation;

import com.capstone.cschatbot.cs.domain.ChatEvaluation;

import java.util.concurrent.CompletableFuture;

public interface EvaluationService {
    CompletableFuture<ChatEvaluation> getEvaluation(String question, String clientAnswer);
}
