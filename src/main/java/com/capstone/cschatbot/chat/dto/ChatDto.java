package com.capstone.cschatbot.chat.dto;

import lombok.*;

public class ChatDto {
    public static class Self_Request {
        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Chat {
            private String question;
            private String content;
        }
    }

    public static class Request {
        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Chat {
            private String prompt;

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
