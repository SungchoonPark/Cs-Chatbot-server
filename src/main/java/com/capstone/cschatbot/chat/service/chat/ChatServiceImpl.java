package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;
import com.capstone.cschatbot.chat.entity.*;
import com.capstone.cschatbot.chat.entity.enums.GPTRoleType;
import com.capstone.cschatbot.chat.service.communicate.CommunicateService;
import com.capstone.cschatbot.chat.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";

    @Value("${openai.model}")
    private String model;

    private final ChatUtil chatUtil;

    private final CommunicateService communicateService;
    private final Map<String, ChatRequest> memberChatMap = new HashMap<>();
    private final Map<String, MemberChatEvaluation> memberChatEvaluationMap = new HashMap<>();

    @Override
    public NewQuestion initiateCSChat(String memberId, String topic) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

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

        Chat chat = Chat.of(
                chatRequest.getMessages().get(chatRequest.getMessages().size()).getContent(), // 질문
                clientAnswer.answer() // 답변
        );
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), clientAnswer.answer());

        String newQuestion = communicateService.withGPT(chatRequest);
        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), newQuestion);

        for (Message message : chatRequest.getMessages()) {
            log.info("role = " + message.getRole() + ", message = " + message.getContent());
        }

        CompletableFuture<Evaluation> evaluationAsync = getEvaluationAsync(clientAnswer);
        evaluationAsync.thenAcceptAsync(evaluation -> {
            ChatEvaluation chatEvaluation = ChatEvaluation.of(chat, evaluation.getEvaluation());
            if(!memberChatEvaluationMap.containsKey(memberId)) {
                MemberChatEvaluation memberChatEvaluation = new MemberChatEvaluation();
                memberChatEvaluation.addNewChatEvaluation(chatEvaluation);
                memberChatEvaluationMap.put(memberId, memberChatEvaluation);
            }

            memberChatEvaluationMap.get(memberId).addNewChatEvaluation(chatEvaluation);

            for (ChatEvaluation chatEvaluation1 : memberChatEvaluationMap.get(memberId).getChatEvaluations()) {
                log.info("question : {} \n answer : {} \n evaluation : {}", chatEvaluation1.getChat().getQuestion(), chatEvaluation1.getChat().getAnswer(), chatEvaluation1.getEvaluation());
            }
        });

        return NewQuestion.builder()
                .question(newQuestion)
                .build();
    }

    @Override
    public NewQuestion testProcessChat(String memberId, ClientAnswer clientAnswer) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        ChatRequest chatRequest = memberChatMap.get(memberId);
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), clientAnswer.answer());

        String newQuestion = communicateService.withGPT(chatRequest);
        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), newQuestion);
        for (Message message : chatRequest.getMessages()) {
            log.info("role = " + message.getRole() + ", message = " + message.getContent());
        }

        return NewQuestion.builder()
                .question(newQuestion)
                .build();

    }

    @Override
    public void terminateChat(String memberId) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        memberChatMap.remove(memberId);
    }

    private NewQuestion initiateChatWithGPT(String memberId, ChatRequest chatRequest) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = communicateService.withGPT(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberChatMap.put(memberId, chatRequest);

        return NewQuestion.builder()
                .question(question)
                .build();
    }

    private CompletableFuture<Evaluation> getEvaluationAsync(ClientAnswer clientAnswer) {
        return CompletableFuture.completedFuture(communicateService.withEvaluationServer(clientAnswer.answer()));
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }
}
