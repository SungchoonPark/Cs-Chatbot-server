package com.capstone.cschatbot.chat.controller;

import com.capstone.cschatbot.chat.dto.ChatDto;
import com.capstone.cschatbot.chat.service.chat.ChatService;
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
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> initiateCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("topic") String topic) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.initiateCSChat(principalDetails.getMemberId(), topic),
                CustomResponseStatus.SUCCESS)
        );
    }

    @GetMapping("/self/initial/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.Chat>> initiateSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute ChatDto.Request.SelfIntroChat chat
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.initiateSelfIntroChat(principalDetails.getMemberId(), chat),
                CustomResponseStatus.SUCCESS)
        );
    }

    @GetMapping("/chat")
    public ResponseEntity<ApiResponse<ChatDto.Response.EvaluationAndQuestion>> processChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam ChatDto.Request.Chat prompt
    ) {
        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.processChat(principalDetails.getMemberId(), prompt),
                CustomResponseStatus.SUCCESS)
        );
    }

    // TODO : 채팅 종료시 답변 평가를 보여준다면 로직이 많이 변할 예정
    @PostMapping("/end/chat")
    public ResponseEntity<ApiResponse<String>> terminateChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        chatService.terminateChat(principalDetails.getMemberId());
        return ResponseEntity.ok().body(ApiResponse.createSuccess("채팅이 종료되었습니다.", CustomResponseStatus.SUCCESS));
    }

}
