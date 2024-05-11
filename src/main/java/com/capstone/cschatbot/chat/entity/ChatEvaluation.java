package com.capstone.cschatbot.chat.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatEvaluation {
    private Chat chat;
    private String evaluation;

    public static ChatEvaluation of(Chat chat, String evaluation) {
        return new ChatEvaluation(chat, evaluation);
    }
}
