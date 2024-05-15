package com.capstone.cschatbot.cs.controller;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.common.dto.ApiResponse;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.config.security.service.PrincipalDetails;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.CSChatHistoryList;
import com.capstone.cschatbot.cs.dto.response.NewQuestion;
import com.capstone.cschatbot.cs.service.CSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Slf4j
public class CSController {
    private final CSService csService;
    // CS 첫 채팅 API
    @GetMapping("/initial/chat/cs")
    public ResponseEntity<ApiResponse<QuestionAndChatId>> initiateCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("topic") String topic) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                csService.initiateCSChat(principalDetails.getMemberId(), topic),
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
                csService.processCSChat(principalDetails.getMemberId(), clientAnswer),
                CustomResponseStatus.SUCCESS)
        );
    }

    // CS 채팅 종료
    @PostMapping("/end/chat/cs/{chatId}")
    public ResponseEntity<ApiResponse<CSChatHistory>> terminateCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId
    ) {
        CSChatHistory response = csService.terminateCSChat(principalDetails.getMemberId(), chatId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/chats/cs")
    public ResponseEntity<ApiResponse<CSChatHistoryList>> findAllCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        CSChatHistoryList response = csService.findAllCSChat(principalDetails.getMemberId());
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }
}
