package com.capstone.cschatbot.selfIntro.application;

import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatInfo;
import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.selfIntro.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.common.dto.QuestionAndChatId;
import com.capstone.cschatbot.common.dto.gpt.ChatRequest;
import com.capstone.cschatbot.common.enums.GPTRoleType;
import com.capstone.cschatbot.common.application.GPTService;
import com.capstone.cschatbot.common.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.selfIntro.domain.SelfIntro;
import com.capstone.cschatbot.selfIntro.dto.SelfIntroChat;
import com.capstone.cschatbot.selfIntro.infrastructure.SelfIntroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SelfIntroServiceImpl implements SelfIntroService {
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";
    private static final String SPLIT_WORD = "Score: ";
    private final ChatUtil chatUtil;
    private final SelfIntroRepository selfIntroRepository;
    private final GPTService gptService;
    private final Map<String, ChatRequest> memberSelfIntroChatMap = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public QuestionAndChatId initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat) {
        ChatRequest chatRequest = ChatRequest.createDefault();
        addSystemInitialPromptToChatMap(chatRequest, chatUtil.createSelfIntroInitialPrompt(chat));
        return initiateSelfIntroChatWithGPT(memberId, chatRequest);
    }

    @Override
    @Transactional
    public NewQuestionAndGrade processSelfIntroChat(String memberId, SelfIntroChatInfo selfIntroChatInfo) {
        validateMember(memberId);

        ChatRequest chatRequest = memberSelfIntroChatMap.get(memberId);

        String question = chatRequest.findRecentQuestion();
        String answer = selfIntroChatInfo.clientAnswer();
        addMemberAnswerToChatMap(chatRequest, answer);

        return processNewQuestionAndGrade(
                gptService.getNewQuestion(chatRequest),
                chatRequest,
                question,
                answer,
                getSelfIntroByChatRoomId(selfIntroChatInfo.chatRoomId())
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "SelfIntros", key = "'selfAll' + #memberId", cacheManager = "oidcCacheManager")
    public void terminateSelfIntroChat(String memberId, String chatRoomId) {
        validateMember(memberId);

        SelfIntro selfIntro = getSelfIntroByChatRoomId(chatRoomId);
        selfIntro.terminateSelfIntroChat();
        selfIntroRepository.save(selfIntro);

        memberSelfIntroChatMap.remove(memberId);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "SelfIntros", key = "'selfAll' + #memberId", cacheManager = "oidcCacheManager"),
            @CacheEvict(value = "SelfIntros", key = "#chatRoomId", cacheManager = "oidcCacheManager")
    })
    public void deleteSelfIntroChat(String memberId, String chatRoomId) {
        SelfIntro selfIntro = getSelfIntroByChatRoomId(chatRoomId);
        selfIntro.checkEqualMember(memberId);

        selfIntroRepository.deleteById(chatRoomId);
    }

    private NewQuestionAndGrade processNewQuestionAndGrade(String newQuestion, ChatRequest chatRequest, String question, String answer, SelfIntro selfIntro) {
        // "Score: "를 기준으로 문자열을 분할하여 평가와 점수를 분리
        String[] parts = newQuestion.split(SPLIT_WORD);

        // 평가와 점수를 각각 따로 저장
        String processedQuestion = parts[0].trim();
        String gradeOfAnswer = parts[1].trim();

        addGPTQuestionToChatMap(chatRequest, processedQuestion);

        selfIntro.addSelfIntroChat(SelfIntroChat.of(question, answer, gradeOfAnswer));
        selfIntroRepository.save(selfIntro);

        return NewQuestionAndGrade.builder()
                .question(processedQuestion)
                .grade(gradeOfAnswer)
                .build();
    }

    private void addMemberAnswerToChatMap(ChatRequest chatRequest, String answer) {
        addMessageToMap(chatRequest, GPTRoleType.USER, answer);
    }

    private void addGPTQuestionToChatMap(ChatRequest chatRequest, String question) {
        addMessageToMap(chatRequest, GPTRoleType.ASSISTANT, question);
    }

    private void addSystemInitialPromptToChatMap(ChatRequest chatRequest, String prompt) {
        addMessageToMap(chatRequest, GPTRoleType.SYSTEM, prompt);
    }

    private void addMessageToMap(ChatRequest chatRequest, GPTRoleType gptRoleType, String message) {
        chatRequest.addMessage(gptRoleType.getRole(), message);
    }

    private SelfIntro getSelfIntroByChatRoomId(String chatRoomId) {
        return selfIntroRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));
    }

    private QuestionAndChatId initiateSelfIntroChatWithGPT(String memberId, ChatRequest chatRequest) {
        addMemberAnswerToChatMap(chatRequest, INITIAL_USER_MESSAGE);

        String question = gptService.getNewQuestion(chatRequest);
        addGPTQuestionToChatMap(chatRequest, question);

        memberSelfIntroChatMap.put(memberId, chatRequest);

        return QuestionAndChatId.builder()
                .question(question)
                .chatRoomId(selfIntroRepository.save(SelfIntro.of(memberId)).getId())
                .build();
    }

    private void validateMember(String memberId) {
        if (!memberSelfIntroChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
    }
}
