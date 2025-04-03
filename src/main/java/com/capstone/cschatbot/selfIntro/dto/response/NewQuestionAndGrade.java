package com.capstone.cschatbot.selfIntro.dto.response;

import lombok.Builder;

@Builder
public record NewQuestionAndGrade(String question, String grade) {
}
