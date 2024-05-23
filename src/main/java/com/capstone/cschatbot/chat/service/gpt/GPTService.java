package com.capstone.cschatbot.chat.service.gpt;

import com.capstone.cschatbot.chat.domain.gpt.ChatRequest;

public interface GPTService {
    String getNewQuestion(ChatRequest chatRequest);
}
