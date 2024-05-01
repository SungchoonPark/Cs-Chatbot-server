package com.capstone.cschatbot.chat.service;

import com.capstone.cschatbot.chat.dto.ChatDto;
import com.capstone.cschatbot.chat.entity.ChatRequest;
import com.capstone.cschatbot.chat.entity.ChatResponse;
import com.capstone.cschatbot.chat.entity.Message;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private static final String USER = "user";
    private static final String ASSISTANT = "assistant";
    private static final String SYSTEM = "system";
    private static final String INITIAL_USER_MESSAGE = "안녕하십니까. 잘 부탁드립니다.";

    @Value("${openai.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Qualifier("chatRestTemplate")
    @Autowired
    private final RestTemplate restTemplate;
    private final Map<String, ChatRequest> memberChatMap = new HashMap<>();

    @Override
    public ChatDto.Response.Chat csInitChat(String memberId, String topic) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, SYSTEM, createInitialPrompt(topic));
        return chatWithGPT(memberId, chatRequest);
    }

    @Override
    public ChatDto.Response.Chat selfInitChat(String memberId, ChatDto.Request.SelfChat chat) {
        if (memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.ALREADY_MAP_EXIST);
        }

        ChatRequest chatRequest = ChatRequest.of(model, 1, 256, 1, 0, 0);
        addChatMessage(chatRequest, SYSTEM, createSelfInitialPrompt(chat));
        return chatWithGPT(memberId, chatRequest);
    }

    @Override
    public ChatDto.Response.Chat chat(String memberId, ChatDto.Request.Chat prompt) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        ChatRequest chatRequest = memberChatMap.get(memberId);
        addChatMessage(chatRequest, USER, prompt.getPrompt());

        ChatResponse response = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        checkValidResponse(response);

        String responseContent = response.getChoices().get(0).getMessage().getContent();

        addChatMessage(chatRequest, ASSISTANT, responseContent);
        for (Message message : chatRequest.getMessages()) {
            log.info("role = " + message.getRole() + ", message = " + message.getContent());
        }

        return ChatDto.Response.Chat.from(responseContent);
    }

    @Override
    public void endChat(String memberId) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        memberChatMap.remove(memberId);
    }

    private ChatDto.Response.Chat chatWithGPT(String memberId, ChatRequest chatRequest) {
        addChatMessage(chatRequest, USER, INITIAL_USER_MESSAGE);

        ChatResponse response = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        checkValidResponse(response);

        String content = response.getChoices().get(0).getMessage().getContent();

        addChatMessage(chatRequest, ASSISTANT, content);
        memberChatMap.put(memberId, chatRequest);

        return ChatDto.Response.Chat.from(content);
    }

    private void addChatMessage(ChatRequest chatRequest, String role, String message) {
        chatRequest.addMessage(role, message);
    }

    private void checkValidResponse(ChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new CustomException(CustomResponseStatus.GPT_NOT_ANSWER);
        }
    }

    private String createInitialPrompt(String topic) {
        String formattedTopic = String.format("\"%s\"", topic);
        return """
                    "너는 네이버 백엔드 개발자 채용을 담당하는 면접관이다."
                    "나는 대학교 4학년이고 컴퓨터 공학을 전공했다."
                    "너는 내가 %1$s 전반에 대한 지식을 잘 알고 있는지 확인을 해야 한다."
                    "나한테 %1$s 관련 질문을 해라"
                    "내가 질문에 답변을 하면 내가 답변을 한 내용을 바탕으로 또 다른 %1$s 관련 지식을 확인하는 질문을 해야 한다."
                    "그리고 대화를 실제 면접 상황이라고 가정하고 질문을 해라"
                    ###Instruction###
                    내 답변 내용을 바탕으로 다른 %1$s 관련 지식을 확인하는 질문을 한다.
                    ###Example###
                    cpu 스케줄링이란 실행 준비 상태의 스레드 중 하나를 선택하는 과정입니다.
                    (response : 그러면 cpu 스케줄링은 언제 실행되나요?)"
                    ###Instruction###
                    너는 나의 %1$s 지식을 확인하는 질문을 한다.
                    ###Example###
                    안녕하십니까.
                    (Response : 반갑습니다. 앞으로 cs관련 질문을 좀 드리겠습니다. %1$s 주요 기능에는 무엇이 있습니까?)"
                    ###Instruction###
                    너는 나의 알고리즘 지식을 확인하는 질문을 한다.
                    ###Example###
                    안녕하십니까.
                    (Response : 반갑습니다. 앞으로 알고리즘 관련 질문을 좀 드리겠습니다. bfs 알고리즘 대해 설명 가능하실까요?)"
                    "%1$s 관련 질문을 잘 하면 너에게 팁을 300k 줄게"
                    "나에게 %1$s 관련 질문을 잘 못하면 너는 불이익을 받을 것입니다."
                    "너는 나에게 100개 이상의 질문을 해야 한다.";
                """.formatted(formattedTopic);
    }


    private String createSelfInitialPrompt(ChatDto.Request.SelfChat self) {
        return """
                    "저는 자기소개서 관련 질문을 하는 면접관이다."
                    "자기소개서의 질문은 "%s"이다."
                    "내 답변은 "%s"이다."
                    "이제 내 답변을 기반으로 자기소개서 관련 질문을 해라."
                    "질문은 적어도 100개는 한다."
                    "질문은 한 번에 하나의 질문만 한다."
                """.formatted(self.getQuestion(), self.getContent());

    }
}
