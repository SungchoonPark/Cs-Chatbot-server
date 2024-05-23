package com.capstone.cschatbot.chat.domain.gpt;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatRequest {
    private final String model;
    private final List<Message> messages;
    private final int temperature;
    private final int max_tokens;
    private final int top_p;
    private final int frequency_penalty;
    private final int presence_penalty;

    private ChatRequest(String model, int temperature, int maxTokens, int topP, int frequencyPenalty, int presencePenalty) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.temperature = temperature;
        this.max_tokens = maxTokens;
        this.top_p = topP;
        this.frequency_penalty = frequencyPenalty;
        this.presence_penalty = presencePenalty;
    }

    public static ChatRequest createDefault() {
        return new ChatRequest("gpt-3.5-turbo", 1, 256, 1, 0, 0);
    }

    public static ChatRequest of(String model, int temperature, int maxTokens, int topP, int frequencyPenalty, int presencePenalty) {
        return new ChatRequest(model, temperature, maxTokens, topP, frequencyPenalty, presencePenalty);
    }

    public void addMessage(String role, String responseContent) {
        this.messages.add(new Message(role, responseContent));
    }

    public String findRecentQuestion() {
        return messages.get(messages.size() - 1).getContent();
    }
}


