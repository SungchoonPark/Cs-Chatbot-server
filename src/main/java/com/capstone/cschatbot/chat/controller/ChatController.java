package com.capstone.cschatbot.chat.controller;

import com.capstone.cschatbot.chat.dto.ChatDto;
import com.capstone.cschatbot.chat.service.ChatService;
import com.capstone.cschatbot.common.dto.ApiResponse;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /***
     * 여기서 시작 주제를 선택해야 하지 않나?
     */
//    @GetMapping("/initial/chat")
//    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> initialChat() {
//        ChatDto.Response.Chat response = chatService.initialChat();
//        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
//    }

    @GetMapping("/initial/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> initialChat(@RequestParam("topic") String topic) {
        ChatDto.Response.Chat response = chatService.initialChat(topic);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> chat(@RequestParam ChatDto.Request.Chat prompt) {
        ChatDto.Response.Chat response = chatService.chat(prompt);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }
}
