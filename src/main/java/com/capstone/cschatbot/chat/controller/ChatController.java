package com.capstone.cschatbot.chat.controller;

import com.capstone.cschatbot.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
}
