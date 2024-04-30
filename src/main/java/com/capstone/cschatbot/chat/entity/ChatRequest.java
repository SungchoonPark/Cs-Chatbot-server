package com.capstone.cschatbot.chat.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private final int n = 1;
    private int temperature;
    private int max_tokens;
    private int top_p;
    private int frequency_penalty;
    private int presence_penalty;


    public ChatRequest(String model, int temperature, int max_tokens, int top_p, int frequency_penalty, int presence_penalty) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.temperature = temperature;
        this.max_tokens = max_tokens;
        this.top_p = top_p;
        this.frequency_penalty = frequency_penalty;
        this.presence_penalty = presence_penalty;
    }

    public void addMessage(String role, String responseContent) {
        this.messages.add(new Message(role, responseContent));
    }
}


