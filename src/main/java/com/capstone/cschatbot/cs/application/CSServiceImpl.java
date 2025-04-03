package com.capstone.cschatbot.cs.application;

import com.capstone.cschatbot.cs.dto.request.ClientAnswer;
import com.capstone.cschatbot.common.dto.QuestionAndChatId;
import com.capstone.cschatbot.common.dto.gpt.ChatRequest;
import com.capstone.cschatbot.common.enums.GPTRoleType;
import com.capstone.cschatbot.common.application.GPTService;
import com.capstone.cschatbot.cs.application.evaluation.EvaluationService;
import com.capstone.cschatbot.common.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.cs.dto.request.CSChatInfo;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.NewQuestion;
import com.capstone.cschatbot.cs.domain.CSChat;
import com.capstone.cschatbot.cs.dto.ChatEvaluation;
import com.capstone.cschatbot.cs.infrastructure.CSChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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

        // GPT와의 대화 축적 인스턴스 생성
        ChatRequest chatRequest = ChatRequest.createDefault();
        // 프롬프팅 메시지 추가
        addSystemInitialPromptToChatMap(chatRequest, chatUtil.createCSInitialPrompt(topic));

        // 새로운 채팅방을 DB에 저장하고 GPT로부터 질문 생성
        return initiateCSChatWithGPT(memberId, chatRequest, topic);
    }

    @Override
    @Transactional
    public NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer) {
        validateMember(memberId);

        ChatRequest chatRequest = getChatRequestByMemberId(memberId);
        String question = chatRequest.findRecentQuestion();
        String answer = clientAnswer.answer();

        // 평가서버로부터 평가를 받아오는 로직은 비동기 방식으로 처리
        addEvaluationWithAsync(memberId, question, answer);
        // 클라이언트의 답변을 채팅내역 저장 Map에 저장
        addMemberAnswerToChatMap(chatRequest, answer);
        // 새로운 질문을 생성해서 리턴
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
        csChatRepository.findById(csChatInfo.chatRoomId())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_MATCH))
                .checkEqualMember(memberId);

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

        // GPT로부터 첫 질문 받아옴
        String question = generateAndAddNewQuestion(chatRequest);
        memberCSChatMap.put(memberId, chatRequest);

        // 새로 생긴 채팅방을 DB에 저장
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

    private void validAccumulatedEvaluations(List<CompletableFuture<ChatEvaluation>> accumulatedEvaluationsWithAsync) {
        if (accumulatedEvaluationsWithAsync == null || accumulatedEvaluationsWithAsync.isEmpty()) {
            throw new CustomException(CustomResponseStatus.EVALUATION_NOT_FOUND);
        }
    }
}
