package com.capstone.cschatbot.selfIntro.controller;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.selfIntro.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;
import com.capstone.cschatbot.common.dto.ApiResponse;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.config.security.service.PrincipalDetails;
import com.capstone.cschatbot.selfIntro.service.SelfIntroService;
import com.capstone.cschatbot.selfIntro.service.query.SelfIntroQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class SelfIntroController {
    private final SelfIntroService selfIntroService;
    private final SelfIntroQueryService selfIntroQueryService;

    // 자소서 채팅 시작
    @GetMapping("/initial/chat/self_intro")
    public ResponseEntity<ApiResponse<QuestionAndChatId>> initiateSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute SelfIntroChatRequest chat
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                selfIntroService.initiateSelfIntroChat(principalDetails.getMemberId(), chat),
                CustomResponseStatus.SUCCESS)
        );
    }

    // 자소서 채팅 ing
    @GetMapping("/chat/self_intro/{chatId}")
    public ResponseEntity<ApiResponse<NewQuestionAndGrade>> processSelfChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId,
            @RequestParam(name = "client_answer") ClientAnswer clientAnswer
    ) {

        return ResponseEntity.ok().body(ApiResponse.createSuccess(
                selfIntroService.processSelfIntroChat(principalDetails.getMemberId(), clientAnswer, chatId),
                CustomResponseStatus.SUCCESS)
        );
    }

    // 자소서 채팅 종료
    @PostMapping("/end/chat/self_intro/{chatId}")
    public ResponseEntity<ApiResponse<String>> terminateSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId
    ) {
        selfIntroService.terminateSelfIntroChat(principalDetails.getMemberId(), chatId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess("채팅이 종료되었습니다.", CustomResponseStatus.SUCCESS));
    }

    // 자소서 채팅 전체 조회
    @GetMapping("/chats/self_intro")
    public ResponseEntity<ApiResponse<SelfIntroList>> getAllSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        SelfIntroList response = selfIntroQueryService.findAllSelfIntro(principalDetails.getMemberId());
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/self_intro/{chatId}")
    public ResponseEntity<ApiResponse<SelfIntroDetail>> getSelfIntroChat(
            @PathVariable String chatId
    ) {
        SelfIntroDetail response = selfIntroQueryService.findSelfIntro(chatId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @DeleteMapping("/self_intro/{chatId}")
    public ResponseEntity<ApiResponse<String>> deleteSelfIntroChat(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String chatId
    ) {
        selfIntroService.deleteSelfIntroChat(principalDetails.getMemberId(), chatId);
        return ResponseEntity.ok().body(ApiResponse.createSuccess("null", CustomResponseStatus.SUCCESS));
    }
}
