package com.capstone.cschatbot.chat.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor()
public class MemberChatEvaluation {
    private List<ChatEvaluation> chatEvaluations = new ArrayList<>();

    public void addNewChatEvaluation(ChatEvaluation chatEvaluation) {
        chatEvaluations.add(chatEvaluation);
    }
}
