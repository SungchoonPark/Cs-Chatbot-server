package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;
import com.capstone.cschatbot.chat.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.chat.entity.*;
import com.capstone.cschatbot.chat.entity.enums.GPTRoleType;
import com.capstone.cschatbot.chat.repository.ChatRepository;
import com.capstone.cschatbot.chat.repository.SelfIntroRepository;
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

    private final ChatRepository chatRepository;

    private final SelfIntroRepository selfIntroRepository;
    private final Map<String, ChatRequest> memberChatMap = new HashMap<>();
    private final Map<String, List<CompletableFuture<ChatEvaluation>>> memberEvaluations = new ConcurrentHashMap<>();

    @Override
    public QuestionAndChatId initiateCSChat(String memberId, String topic) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }
        if (memberEvaluations.containsKey(memberId)) {
            throw new CustomException((CustomResponseStatus.ALREADY_EVALUATION_MAP_EXIST));
        }

        memberEvaluations.put(memberId, new LinkedList<>());
        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createCSInitialPrompt(topic));

        return initiateCSChatWithGPT(memberId, chatRequest, topic);
    }

    @Override
    public QuestionAndChatId initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createSelfIntroInitialPrompt(chat));
        return initiateSelfIntroChatWithGPT(memberId, chatRequest);
    }

    @Override
    public NewQuestion processCSChat(String memberId, ClientAnswer clientAnswer) {
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

    @Override
    public NewQuestionAndGrade processSelfIntroChat(String memberId, ClientAnswer clientAnswer, String chatRoomId) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));

        ChatRequest chatRequest = memberChatMap.get(memberId);
        String question = chatRequest.getMessages().get(chatRequest.getMessages().size() - 1).getContent();
        String answer = clientAnswer.answer();

        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), clientAnswer.answer());

        String newQuestion = GPTService.getNewQuestion(chatRequest);

        // "Score: "를 기준으로 문자열을 분할하여 평가와 점수를 분리
        String[] parts = newQuestion.split("Score: ");

        // 평가와 점수를 각각 따로 저장
        String newQ = parts[0].trim();
        String grade = parts[1].trim();

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), newQ);

        SelfIntroChat selfIntroChat = SelfIntroChat.of(question, answer, grade);
        selfIntro.addSelfIntroChat(selfIntroChat);
        selfIntroRepository.save(selfIntro);

        return NewQuestionAndGrade.builder()
                .question(newQ)
                .grade(grade)
                .build();
    }

    @Override
    public void terminateCSChat(String memberId, String chatId) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
        List<CompletableFuture<ChatEvaluation>> evaluations = memberEvaluations.remove(memberId);

        List<ChatEvaluation> chatEvaluations = new ArrayList<>();
        CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(evaluations.toArray(new CompletableFuture[0]));
        log.info("id : {}", chatId);

        CSChat CSChat = chatRepository.findById(chatId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.CS_CHAT_NOT_FOUND));

        allOfFuture.thenRun(() -> {
            log.info("[평가 끝] 모든 비동기 요청 작업 종료");
            for (CompletableFuture<ChatEvaluation> evaluation : evaluations) {
                evaluation.thenAccept(chatEvaluation -> {
                    log.info("질문 : {}", chatEvaluation.getQuestion());
                    log.info("답변 : {}", chatEvaluation.getAnswer());
                    log.info("평가 결과: {}", chatEvaluation.getEvaluation().getEvaluation());
                    chatEvaluations.add(chatEvaluation);
                });
            }
            CSChat.updateChatHistory(chatEvaluations);
            chatRepository.save(CSChat);
            memberChatMap.remove(memberId);
        });
    }

    @Override
    public void terminateSelfIntroChat(String memberId, String chatRoomId) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId).orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));
        selfIntro.terminateSelfIntroChat();
        selfIntroRepository.save(selfIntro);
        memberChatMap.remove(memberId);
    }

    private QuestionAndChatId initiateCSChatWithGPT(String memberId, ChatRequest chatRequest, String topic) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = GPTService.getNewQuestion(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberChatMap.put(memberId, chatRequest);

        CSChat saveCSChat = chatRepository.save(CSChat.of(memberId, topic));

        return QuestionAndChatId.builder()
                .question(question)
                .chatRoomId(saveCSChat.getId())
                .build();
    }

    private QuestionAndChatId initiateSelfIntroChatWithGPT(String memberId, ChatRequest chatRequest) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = GPTService.getNewQuestion(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberChatMap.put(memberId, chatRequest);

        SelfIntro saveSelfIntro = selfIntroRepository.save(SelfIntro.of(memberId));

        return QuestionAndChatId.builder()
                .question(question)
                .chatRoomId(saveSelfIntro.getId())
                .build();
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }
}
