package com.capstone.cschatbot.selfIntro.service;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.selfIntro.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.selfIntro.dto.response.NewQuestionAndGrade;
import com.capstone.cschatbot.chat.dto.response.QuestionAndChatId;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroDetail;
import com.capstone.cschatbot.selfIntro.dto.response.SelfIntroList;
import com.capstone.cschatbot.chat.entity.gpt.ChatRequest;
import com.capstone.cschatbot.chat.entity.enums.GPTRoleType;
import com.capstone.cschatbot.chat.service.gpt.GPTService;
import com.capstone.cschatbot.chat.util.ChatUtil;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import com.capstone.cschatbot.selfIntro.entity.SelfIntroChat;
import com.capstone.cschatbot.selfIntro.repository.SelfIntroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SelfIntroServiceImpl implements SelfIntroService{
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";
    private final ChatUtil chatUtil;
    private final SelfIntroRepository selfIntroRepository;
    private final GPTService gptService;

    @Value("${openai.model}")
    private String model;
    private final Map<String, ChatRequest> memberSelfIntroChatMap = new HashMap<>();

    @Override
    public QuestionAndChatId initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat) {
        if (memberSelfIntroChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, GPTRoleType.SYSTEM.getRole(), chatUtil.createSelfIntroInitialPrompt(chat));
        return initiateSelfIntroChatWithGPT(memberId, chatRequest);
    }

    @Override
    public NewQuestionAndGrade processSelfIntroChat(String memberId, ClientAnswer clientAnswer, String chatRoomId) {
        if (!memberSelfIntroChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }
        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));

        ChatRequest chatRequest = memberSelfIntroChatMap.get(memberId);
        String question = chatRequest.getMessages().get(chatRequest.getMessages().size() - 1).getContent();
        String answer = clientAnswer.answer();

        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), clientAnswer.answer());

        String newQuestion = gptService.getNewQuestion(chatRequest);

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
    public void terminateSelfIntroChat(String memberId, String chatRoomId) {
        if (!memberSelfIntroChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId).orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));
        selfIntro.terminateSelfIntroChat();
        selfIntroRepository.save(selfIntro);
        memberSelfIntroChatMap.remove(memberId);
    }

    @Override
    public SelfIntroList findAllSelfIntro(String memberId) {
        List<SelfIntro> selfIntros = selfIntroRepository.findAllByMemberIdAndTerminateStatusTrue(memberId);
        return SelfIntroList.builder()
                .selfIntros(selfIntros)
                .build();
    }

    @Override
    public SelfIntroDetail findSelfIntro(String chatRoomId) {
        SelfIntro selfIntro = selfIntroRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.SELF_INTRO_CHAT_NOT_FOUND));

        return SelfIntroDetail.builder()
                .selfIntroChats(selfIntro.getSelfIntroChats())
                .build();
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }

    private QuestionAndChatId initiateSelfIntroChatWithGPT(String memberId, ChatRequest chatRequest) {
        addChatMessage(chatRequest, GPTRoleType.USER.getRole(), INITIAL_USER_MESSAGE);

        String question = gptService.getNewQuestion(chatRequest);

        addChatMessage(chatRequest, GPTRoleType.ASSISTANT.getRole(), question);
        memberSelfIntroChatMap.put(memberId, chatRequest);

        SelfIntro saveSelfIntro = selfIntroRepository.save(SelfIntro.of(memberId));

        return QuestionAndChatId.builder()
                .question(question)
                .chatRoomId(saveSelfIntro.getId())
                .build();
    }
}
