package com.capstone.cschatbot.chat.service.communicate;

import com.capstone.cschatbot.chat.entity.ChatRequest;
import com.capstone.cschatbot.chat.entity.Evaluation;

public interface CommunicateService {

    Evaluation withEvaluationServer(String clientAnswer);

    String withGPT(ChatRequest chatRequest);
}
