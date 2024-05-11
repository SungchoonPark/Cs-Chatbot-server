package com.capstone.cschatbot.chat.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {
    private String question;
    private String answer;

    public static  Chat of(String question, String answer) {
        return new Chat(question, answer);
    }
}
