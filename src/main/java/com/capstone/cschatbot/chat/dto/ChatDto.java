package com.capstone.cschatbot.chat.dto;

import com.capstone.cschatbot.chat.entity.Evaluation;
import lombok.*;

public class ChatDto {

    public static class Request {
        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Chat {
            private String prompt;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class SelfChat {
            private String question;
            private String content;
        }
    }

    public static class Response {
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Chat {
            private String answer;
            public static Chat from(String answer) {
                return Chat.builder()
                        .answer(answer)
                        .build();
            }
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class EvaluationAndQuestion {
            private String evaluation;
            private String question;
            public static EvaluationAndQuestion of(Evaluation evaluation, String question) {
                return EvaluationAndQuestion.builder()
                        .evaluation(evaluation.getEvaluation())
                        .question(question)
                        .build();
            }
        }
    }
}
