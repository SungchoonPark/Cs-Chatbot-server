package com.capstone.cschatbot.cs.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatEvaluation {
    private String question;
    private String answer;
    private String evaluation;

    public static ChatEvaluation of(String question, String answer, String evaluation) {
        return new ChatEvaluation(question, answer, evaluation);
    }
}
