package com.capstone.cschatbot.common.infrastructure;

import com.capstone.cschatbot.common.dto.gpt.ChatRequest;
import com.capstone.cschatbot.common.dto.gpt.ChatResponse;
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
