package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;
import com.capstone.cschatbot.chat.entity.*;
import com.capstone.cschatbot.chat.entity.enums.GPTRoleType;
import com.capstone.cschatbot.chat.service.communicate.GPTService;
import com.capstone.cschatbot.chat.service.evaluation.EvaluationService;
import com.capstone.cschatbot.chat.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";

    @Value("${openai.model}")
    private String model;

    private final ChatUtil chatUtil;

    private final GPTService GPTService;

    private final EvaluationService evaluationService;
    private final Map<String, ChatRequest> memberChatMap = new HashMap<>();
    private final Map<String, List<CompletableFuture<ChatEvaluation>>> memberEvaluations = new ConcurrentHashMap<>();

    @Override
    public NewQuestion initiateCSChat(String memberId, String topic) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }
        if (memberEvaluations.containsKey(memberId)) {
            throw new CustomException((CustomResponseStatus.ALREADY_EVALUATION_MAP_EXIST));
        }

        memberEvaluations.put(memberId, new LinkedList<>());
        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createCSInitialPrompt(topic));
        return initiateChatWithGPT(memberId, chatRequest);
    }

    @Override
    public NewQuestion initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createSelfIntroInitialPrompt(chat));
        return initiateChatWithGPT(memberId, chatRequest);
    }

    @Override
    public NewQuestion processChat(String memberId, ClientAnswer clientAnswer) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        ChatRequest chatRequest = memberChatMap.get(memberId);
        String question = chatRequest.getMessages().get(chatRequest.getMessages().size() - 1).getContent();
        String answer = clientAnswer.answer();
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), clientAnswer.answer());

        String newQuestion = GPTService.getNewQuestion(chatRequest);
        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), newQuestion);

        for (Message message : chatRequest.getMessages()) {
            log.info("role = " + message.getRole() + ", message = " + message.getContent());
        }

        final CompletableFuture<ChatEvaluation> chatEvaluationFuture = evaluationService.getEvaluation(question, answer);
        List<CompletableFuture<ChatEvaluation>> completableFutures = memberEvaluations.computeIfAbsent(memberId, k -> new ArrayList<>());
        completableFutures.add(chatEvaluationFuture);

        return NewQuestion.builder()
                .question(newQuestion)
                .build();
    }

    // TODO : 자소서용 채팅 종료 따로 만들어야함.
    @Override
    public void terminateChat(String memberId) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
        List<CompletableFuture<ChatEvaluation>> evaluations = memberEvaluations.remove(memberId);

        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(evaluations.toArray(new CompletableFuture[0]));
        allOfFuture.thenRun(() -> {
            log.info("[평가 끝] 모든 비동기 요청 작업 종료");
            for (CompletableFuture<ChatEvaluation> evaluation : evaluations) {
                evaluation.thenAccept(chatEvaluation -> {
                    log.info("질문 : {}", chatEvaluation.getQuestion());
                    log.info("답변 : {}", chatEvaluation.getAnswer());
                    log.info("평가 결과: {}", chatEvaluation.getEvaluation().getEvaluation());
                });
            }
        });
        memberChatMap.remove(memberId);
    }

    private NewQuestion initiateChatWithGPT(String memberId, ChatRequest chatRequest) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = GPTService.getNewQuestion(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberChatMap.put(memberId, chatRequest);

        return NewQuestion.builder()
                .question(question)
                .build();
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }
}
