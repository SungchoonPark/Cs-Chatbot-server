package com.capstone.cschatbot.common.application;

import com.capstone.cschatbot.common.dto.gpt.ChatRequest;

public interface GPTService {
    String getNewQuestion(ChatRequest chatRequest);
}
