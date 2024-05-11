package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.ChatResponse;
import com.capstone.cschatbot.chat.dto.response.EvaluationAndQuestionResponse;
import com.capstone.cschatbot.chat.entity.ChatRequest;
import com.capstone.cschatbot.chat.entity.Evaluation;
import com.capstone.cschatbot.chat.entity.Message;
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

    @Override
    public ChatResponse initiateCSChat(String memberId, String topic) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createCSInitialPrompt(topic));
        return initiateChatWithGPT(memberId, chatRequest);
    }

    @Override
    public ChatResponse initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createSelfIntroInitialPrompt(chat));
        return initiateChatWithGPT(memberId, chatRequest);
    }

    @Override
    public EvaluationAndQuestionResponse processChat(String memberId, ClientAnswer chatRequestDto) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        ChatRequest chatRequest = memberChatMap.get(memberId);
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), chatRequestDto.answer());

        Evaluation evaluation = communicateService.withEvaluationServer(chatRequestDto.answer());
        String newQuestion = communicateService.withGPT(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), newQuestion);
        for (Message message : chatRequest.getMessages()) {
            log.info("role = " + message.getRole() + ", message = " + message.getContent());
        }

        return EvaluationAndQuestionResponse.builder()
                .evaluation(evaluation.getEvaluation())
                .question(newQuestion)
                .build();
    }

    @Override
    public ChatResponse testProcessChat(String memberId, ClientAnswer clientAnswer) {
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

        return ChatResponse.builder()
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

    private ChatResponse initiateChatWithGPT(String memberId, ChatRequest chatRequest) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = communicateService.withGPT(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberChatMap.put(memberId, chatRequest);

        return ChatResponse.builder()
                .question(question)
                .build();
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }
}
