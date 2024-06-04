package com.capstone.cschatbot.cs.service;

import com.capstone.cschatbot.chat.domain.gpt.Message;
import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.chat.domain.gpt.ChatRequest;
import com.capstone.cschatbot.chat.domain.enums.GPTRoleType;
import com.capstone.cschatbot.chat.service.gpt.GPTService;
import com.capstone.cschatbot.chat.service.evaluation.EvaluationService;
import com.capstone.cschatbot.chat.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.cs.dto.request.CSChatInfo;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.NewQuestion;
import com.capstone.cschatbot.cs.domain.CSChat;
import com.capstone.cschatbot.cs.domain.ChatEvaluation;
import com.capstone.cschatbot.cs.repository.CSChatRepository;
import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSServiceImpl implements CSService {
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";

    private final ChatUtil chatUtil;

    private final GPTService gptService;

    private final EvaluationService evaluationService;

    private final CSChatRepository csChatRepository;
    private final Map<String, ChatRequest> memberCSChatMap = new ConcurrentHashMap<>();
    private final Map<String, List<CompletableFuture<ChatEvaluation>>> memberEvaluations = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public QuestionAndChatId initiateCSChat(String memberId, String topic) {
        initializeMemberEvaluation(memberId);

        ChatRequest chatRequest = ChatRequest.createDefault();
        addSystemInitialPromptToChatMap(chatRequest, chatUtil.createCSInitialPrompt(topic));

        return initiateCSChatWithGPT(memberId, chatRequest, topic);
    }

    @Override
    @Transactional
    public NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer) {
        validateMember(memberId);

        ChatRequest chatRequest = getChatRequestByMemberId(memberId);
        String question = chatRequest.findRecentQuestion();
        String answer = clientAnswer.answer();

        addEvaluationWithAsync(memberId, question, answer);
        addMemberAnswerToChatMap(chatRequest, answer);

        return NewQuestion.builder()
                .question(generateAndAddNewQuestion(chatRequest))
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CSChats", key = "'csAll:' + #memberId + ':' + #csChatInfo.topic()", cacheManager = "oidcCacheManager"),
            @CacheEvict(value = "CSChats", key = "'csAllTopic:' + #memberId", cacheManager = "oidcCacheManager"),
            @CacheEvict(value = "CSChat", key = "#csChatInfo.chatRoomId()", cacheManager = "oidcCacheManager")
    })
    public CSChatHistory terminateCSChat(String memberId, CSChatInfo csChatInfo) {
        validateMember(memberId);

        List<CompletableFuture<ChatEvaluation>> accumulatedEvaluationsWithAsync = memberEvaluations.remove(memberId);
        validAccumulatedEvaluations(accumulatedEvaluationsWithAsync);

        CompletableFuture<Void> allEvaluationsFuture = CompletableFuture.allOf(accumulatedEvaluationsWithAsync.toArray(new CompletableFuture[0]));

        return completeEvaluationsAndTerminateChat(
                allEvaluationsFuture,
                accumulatedEvaluationsWithAsync,
                csChatInfo.chatRoomId(),
                memberId
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CSChats", key = "'csAll:' + #memberId + ':' + #csChatInfo.topic()", cacheManager = "oidcCacheManager"),
            @CacheEvict(value = "CSChats", key = "'csAllTopic:' + #memberId", cacheManager = "oidcCacheManager"),
            @CacheEvict(value = "CSChat", key = "#csChatInfo.chatRoomId()", cacheManager = "oidcCacheManager")
    })
    public void deleteCSChat(String memberId, CSChatInfo csChatInfo) {
        CSChat csChat = csChatRepository.findById(csChatInfo.chatRoomId())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_MATCH));
        checkEqualMember(memberId, csChat);

        csChatRepository.deleteById(csChatInfo.chatRoomId());
    }

    private CSChatHistory completeEvaluationsAndTerminateChat(
            CompletableFuture<Void> allEvaluationsFuture,
            List<CompletableFuture<ChatEvaluation>> accumulatedEvaluationsWithAsync,
            String chatId,
            String memberId
    ) {
        try {
            // 모든 비동기 작업이 끝나기를 기다리는 join() 메서드
            allEvaluationsFuture.join();
        } catch (CompletionException e) {
            throw new CustomException(CustomResponseStatus.ASYNC_COMPLETION_ERROR);
        }

        List<ChatEvaluation> chatEvaluations = accumulatedEvaluationsWithAsync.stream()
                .map(CompletableFuture::join)
                .toList();

        CSChat csChat = findCSChatByChatId(chatId);
        csChat.terminateProcess(chatEvaluations);
        memberCSChatMap.remove(memberId);

        return CSChatHistory.builder()
                .chatEvaluations(csChatRepository.save(csChat).getChatHistory())
                .build();
    }

    private CSChat findCSChatByChatId(String chatId) {
        return csChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.CS_CHAT_NOT_FOUND));
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

    private void initializeMemberEvaluation(String memberId) {
        memberEvaluations.put(memberId, new LinkedList<>());
    }

    private QuestionAndChatId initiateCSChatWithGPT(String memberId, ChatRequest chatRequest, String topic) {
        addMemberAnswerToChatMap(chatRequest, INITIAL_USER_MESSAGE);

        String question = generateAndAddNewQuestion(chatRequest);
        memberCSChatMap.put(memberId, chatRequest);

        CSChat saveCSChat = csChatRepository.save(CSChat.of(memberId, topic));

        return QuestionAndChatId.builder()
                .question(question)
                .chatRoomId(saveCSChat.getId())
                .build();
    }

    private ChatRequest getChatRequestByMemberId(String memberId) {
        return memberCSChatMap.get(memberId);
    }

    private String generateAndAddNewQuestion(ChatRequest chatRequest) {
        String newQuestion = gptService.getNewQuestion(chatRequest);
        addGPTQuestionToChatMap(chatRequest, newQuestion);
        return newQuestion;
    }

    private void addEvaluationWithAsync(String memberId, String question, String answer) {
        final CompletableFuture<ChatEvaluation> chatEvaluationFuture = evaluationService.getEvaluation(question, answer);
        List<CompletableFuture<ChatEvaluation>> completableFutures = memberEvaluations.computeIfAbsent(memberId, k -> new ArrayList<>());
        completableFutures.add(chatEvaluationFuture);
    }

    private void validateMember(String memberId) {
        if (!memberCSChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
    }

    private static void checkEqualMember(String memberId, CSChat csChat) {
        if (!csChat.getMemberId().equals(memberId)) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_MATCH);
        }
    }

    private void validAccumulatedEvaluations(List<CompletableFuture<ChatEvaluation>> accumulatedEvaluationsWithAsync) {
        if (accumulatedEvaluationsWithAsync == null || accumulatedEvaluationsWithAsync.isEmpty()) {
            throw new CustomException(CustomResponseStatus.EVALUATION_NOT_FOUND);
        }
    }
}
