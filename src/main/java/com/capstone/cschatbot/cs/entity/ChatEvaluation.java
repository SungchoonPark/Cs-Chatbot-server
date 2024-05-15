package com.capstone.cschatbot.cs.entity;

import com.capstone.cschatbot.chat.entity.evaluation.Evaluation;
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
    private Evaluation evaluation;

    public static ChatEvaluation of(String question, String answer, Evaluation evaluation) {
        return new ChatEvaluation(question, answer, evaluation);
    }
}
