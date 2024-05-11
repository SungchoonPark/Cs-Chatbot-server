package com.capstone.cschatbot.chat.controller;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;
import com.capstone.cschatbot.chat.dto.response.EvaluationAndQuestionResponse;
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

    @GetMapping("/initial/chat/cs")
    public ResponseEntity<ApiResponse<NewQuestion>> initiateCSChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam("topic") String topic) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.initiateCSChat(principalDetails.getMemberId(), topic),
                CustomResponseStatus.SUCCESS)
        );
    }

    @GetMapping("/initial/chat/self_intro")
    public ResponseEntity<ApiResponse<NewQuestion>> initiateSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute SelfIntroChatRequest chat
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.initiateSelfIntroChat(principalDetails.getMemberId(), chat),
                CustomResponseStatus.SUCCESS)
        );
    }

    @GetMapping("/chat")
    public ResponseEntity<ApiResponse<NewQuestion>> processChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(name = "client_answer") ClientAnswer clientAnswer
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.processChat(principalDetails.getMemberId(), clientAnswer),
                CustomResponseStatus.SUCCESS)
        );
    }

    /** 백엔드 테스트용 컨트롤러 */
    @GetMapping("/chat/test")
    public ResponseEntity<ApiResponse<NewQuestion>> testProcessChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(name = "client_answer") ClientAnswer clientAnswer
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                chatService.testProcessChat(principalDetails.getMemberId(), clientAnswer),
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
