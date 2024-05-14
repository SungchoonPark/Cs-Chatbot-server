package com.capstone.cschatbot.chat.dto.response;

import lombok.Builder;

@Builder
public record NewQuestionAndGrade(String question, String grade) {
}
