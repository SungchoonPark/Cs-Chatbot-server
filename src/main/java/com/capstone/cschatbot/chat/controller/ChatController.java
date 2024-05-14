package com.capstone.cschatbot.chat.controller;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;
import com.capstone.cschatbot.chat.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
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

    // CS 첫 채팅 API
    @GetMapping("/initial/chat/cs")
    public ResponseEntity<ApiResponse<QuestionAndChatId>> initiateCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("topic") String topic) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.initiateCSChat(principalDetails.getMemberId(), topic),
                CustomResponseStatus.SUCCESS)
        );
    }

    // 자기소개서 첫 채팅 API
    @GetMapping("/initial/chat/self_intro")
    public ResponseEntity<ApiResponse<QuestionAndChatId>> initiateSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute SelfIntroChatRequest chat
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.initiateSelfIntroChat(principalDetails.getMemberId(), chat),
                CustomResponseStatus.SUCCESS)
        );
    }

    // CS 채팅 진행 API
    @GetMapping("/chat/cs")
    public ResponseEntity<ApiResponse<NewQuestion>> processCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(name = "client_answer") ClientAnswer clientAnswer
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.processCSChat(principalDetails.getMemberId(), clientAnswer),
                CustomResponseStatus.SUCCESS)
        );
    }

    // 자기소개서 채팅 진행 API
    @GetMapping("/chat/self_intro/{chatId}")
    public ResponseEntity<ApiResponse<NewQuestionAndGrade>> processSelfChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId,
            @RequestParam(name = "client_answer") ClientAnswer clientAnswer
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.processSelfIntroChat(principalDetails.getMemberId(), clientAnswer, chatId),
                CustomResponseStatus.SUCCESS)
        );
    }

    // CS 채팅 종료
    // TODO : 여기선 평가 정보를 보여줘야 함.
    @PostMapping("/end/chat/cs/{chatId}")
    public ResponseEntity<ApiResponse<String>> terminateCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId
    ) {
        chatService.terminateCSChat(principalDetails.getMemberId(), chatId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess("채팅이 종료되었습니다.", CustomResponseStatus.SUCCESS));
    }

    // 자기소개서 채팅 종료
    @PostMapping("/end/chat/self_intro/{chatId}")
    public ResponseEntity<ApiResponse<String>> terminateSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId
    ) {
        chatService.terminateSelfIntroChat(principalDetails.getMemberId(), chatId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess("채팅이 종료되었습니다.", CustomResponseStatus.SUCCESS));
    }

}
