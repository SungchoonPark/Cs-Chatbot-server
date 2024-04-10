package com.capstone.cschatbot.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class ChatResponse {
    private List<Choice> choices;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Choice {

        private int index;
        private Message message;
    }
}
