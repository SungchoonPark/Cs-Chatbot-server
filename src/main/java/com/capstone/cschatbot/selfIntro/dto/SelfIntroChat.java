package com.capstone.cschatbot.selfIntro.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SelfIntroChat {
    private String question;
    private String answer;
    private String grade;

    public static SelfIntroChat of(String question, String answer, String grade) {
        return new SelfIntroChat(question, answer, grade);
    }
}
