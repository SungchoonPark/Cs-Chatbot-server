package com.capstone.cschatbot.cs.service;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.chat.entity.gpt.ChatRequest;
import com.capstone.cschatbot.chat.entity.gpt.Message;
import com.capstone.cschatbot.chat.entity.enums.GPTRoleType;
import com.capstone.cschatbot.chat.service.gpt.GPTService;
import com.capstone.cschatbot.chat.service.evaluation.EvaluationService;
import com.capstone.cschatbot.chat.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.cs.dto.response.CSChatHistory;
import com.capstone.cschatbot.cs.dto.response.NewQuestion;
import com.capstone.cschatbot.cs.entity.CSChat;
import com.capstone.cschatbot.cs.entity.ChatEvaluation;
import com.capstone.cschatbot.cs.repository.CSChatRepository;
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
public class CSServiceImpl implements CSService {
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";

    @Value("${openai.model}")
    private String model;

    private final ChatUtil chatUtil;

    private final GPTService gptService;

    private final EvaluationService evaluationService;

    private final CSChatRepository csChatRepository;

    private final Map<String, ChatRequest> memberCSChatMap = new HashMap<>();
    private final Map<String, List<CompletableFuture<ChatEvaluation>>> memberEvaluations = new ConcurrentHashMap<>();

    @Override
    public QuestionAndChatId initiateCSChat(String memberId, String topic) {
        if (memberCSChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }
        if (memberEvaluations.containsKey(memberId)) {
            throw new CustomException((CustomResponseStatus.ALREADY_EVALUATION_MAP_EXIST));
        }

        memberEvaluations.put(memberId, new LinkedList<>());
        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);

        addChatMessage(
                chatRequest,
                GPTRoleType.SYSTEM.getRole(),
                chatUtil.createCSInitialPrompt(topic)
        );

        return initiateCSChatWithGPT(memberId, chatRequest, topic);
    }

    @Override
    public NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer) {
        if (!memberCSChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        ChatRequest chatRequest = memberCSChatMap.get(memberId);
        String question = chatRequest.getMessages().get(chatRequest.getMessages().size() - 1).getContent();
        String answer = clientAnswer.answer();
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), clientAnswer.answer());

        String newQuestion = gptService.getNewQuestion(chatRequest);
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

    @Override
    public CSChatHistory terminateCSChat(String memberId, String chatId) {
        if (!memberCSChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
        List<CompletableFuture<ChatEvaluation>> evaluations = memberEvaluations.remove(memberId);

        List<ChatEvaluation> chatEvaluations = new ArrayList<>();
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(evaluations.toArray(new CompletableFuture[0]));

        CSChat csChat = csChatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.CS_CHAT_NOT_FOUND));

        return allOfFuture.thenApply(v -> {
            log.info("[평가 끝] 모든 비동기 요청 작업 종료");
            evaluations.forEach(e -> e.thenAccept(chatEvaluations::add));

            csChat.updateChatHistory(chatEvaluations);
            CSChat save = csChatRepository.save(csChat);
            memberCSChatMap.remove(memberId);

            return CSChatHistory.builder()
                    .csChat(save)
                    .build();
        }).join();
    }

    private QuestionAndChatId initiateCSChatWithGPT(String memberId, ChatRequest chatRequest, String topic) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = gptService.getNewQuestion(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberCSChatMap.put(memberId, chatRequest);

        CSChat saveCSChat = csChatRepository.save(CSChat.of(memberId, topic));

        return QuestionAndChatId.builder()
                .question(question)
                .chatRoomId(saveCSChat.getId())
                .build();
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }

}
