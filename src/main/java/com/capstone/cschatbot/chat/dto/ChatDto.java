package com.capstone.cschatbot.chat.dto;

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

        @Getter
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
    }
}
