package com.capstone.cschatbot.chat.dto.response;

import lombok.Builder;

@Builder
public record EvaluationAndQuestionResponse(
        String evaluation,
        String question
) {
}
