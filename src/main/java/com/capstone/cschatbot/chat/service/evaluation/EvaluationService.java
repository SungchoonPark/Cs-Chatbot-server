package com.capstone.cschatbot.chat.service.evaluation;

import com.capstone.cschatbot.cs.entity.ChatEvaluation;

import java.util.concurrent.CompletableFuture;

public interface EvaluationService {
    CompletableFuture<ChatEvaluation> getEvaluation(String question, String clientAnswer);
}
