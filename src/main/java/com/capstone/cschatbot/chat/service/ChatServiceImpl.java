package com.capstone.cschatbot.chat.service;

import com.capstone.cschatbot.chat.entity.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private static final String USER = "user";
    private static final String ASSISTANT = "assistant";

    @Value("${openai.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Qualifier("chatRestTemplate")
    @Autowired
    private final RestTemplate restTemplate;

    private ChatRequest chatRequest;
}
