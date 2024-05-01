package com.capstone.cschatbot.chat.controller;

import com.capstone.cschatbot.chat.dto.ChatDto;
import com.capstone.cschatbot.chat.service.ChatService;
import com.capstone.cschatbot.common.dto.ApiResponse;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.config.security.service.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/initial/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> initialChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("topic") String topic) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.csInitChat(principalDetails.getMemberId(), topic),
                CustomResponseStatus.SUCCESS)
        );
    }

    @GetMapping("/self/initial/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> selfInitialChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody ChatDto.Request.SelfChat chat
    ) {
        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.selfInitChat(principalDetails.getMemberId(), chat),
                CustomResponseStatus.SUCCESS)
        );
    }

    @GetMapping("/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> chat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam ChatDto.Request.Chat prompt
    ) {
        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.chat(principalDetails.getMemberId(), prompt),
                CustomResponseStatus.SUCCESS)
        );
    }

    @PostMapping("/end/chat")
    public ResponseEntity<ApiResponse<String>> endChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        chatService.endChat(principalDetails.getMemberId());
        return ResponseEntity.ok().body(ApiResponse.createSuccess("채팅이 종료되었습니다.", CustomResponseStatus.SUCCESS));
    }

}
