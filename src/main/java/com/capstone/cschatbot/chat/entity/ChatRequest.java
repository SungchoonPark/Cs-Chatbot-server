package com.capstone.cschatbot.chat.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private final int n = 1;
    private double temperature;

    public ChatRequest(String model) {
        this.model = model;
        this.messages = new ArrayList<>();
    }

    public void addMessage(String role, String responseContent) {
        this.messages.add(new Message(role, responseContent));
    }
}
