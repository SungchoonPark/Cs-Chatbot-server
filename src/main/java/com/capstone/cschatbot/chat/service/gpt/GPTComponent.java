package com.capstone.cschatbot.chat.service.gpt;

import com.capstone.cschatbot.chat.domain.gpt.ChatRequest;
import com.capstone.cschatbot.chat.domain.gpt.ChatResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Component
@HttpExchange
public interface GPTComponent {

    @PostExchange()
    ChatResponse getGptResponse(@RequestBody ChatRequest request);
}
