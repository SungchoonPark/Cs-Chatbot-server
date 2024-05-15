package com.capstone.cschatbot.selfIntro.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SelfIntroChat {
    private String question;
    private String answer;
    private String grade;

    public static SelfIntroChat of(String question, String answer, String grade) {
        return new SelfIntroChat(question, answer, grade);
    }
}
