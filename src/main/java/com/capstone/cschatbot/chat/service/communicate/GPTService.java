package com.capstone.cschatbot.chat.service.communicate;

import com.capstone.cschatbot.chat.entity.ChatRequest;

public interface GPTService {
    String getNewQuestion(ChatRequest chatRequest);
}
