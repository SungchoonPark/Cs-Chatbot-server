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

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private static final String USER = "user";
    private static final String ASSISTANT = "assistant";
    private static final String SYSTEM = "system";

    @Value("${openai.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Qualifier("chatRestTemplate")
    @Autowired
    private final RestTemplate restTemplate;

    private ChatRequest chatRequest;

    @Override
    public ChatDto.Response.Chat initialChat(String topic) {
        chatRequest = new ChatRequest(model);
        chatRequest.addMessage(SYSTEM, createInitialPrompt(topic));
        chatRequest.addMessage(USER, "안녕하십니까. 이번 네이버 백엔드 개발자에 지원한 이창민이라고"+
                " 합니다. 잘 부탁드립니다.");

        ChatResponse chatResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        log.info("[RESPONSE] : " + chatResponse.getChoices().get(0).getMessage().getContent());
        String content = chatResponse.getChoices().get(0).getMessage().getContent();
        return ChatDto.Response.Chat.from(content);
    }

    @Override
    public ChatDto.Response.Chat chat(ChatDto.Request.Chat prompt) {
        // 유저의 질문에 대한 답변을 List에 저장
        chatRequest.addMessage(USER, prompt.getPrompt());
        log.info("prompt : " + prompt.getPrompt());
        ChatResponse response = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        for (Message message : chatRequest.getMessages()) {
            System.out.println("role = "+ message.getRole() +"message = " + message.getContent());
        }

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new CustomException(CustomResponseStatus.GPT_NOT_ANSWER);
        }
        String responseContent = response.getChoices().get(0).getMessage().getContent();
        log.info("[RESPONSE] : " + responseContent);

        // 앞선 대화를 통해 뽑아낸 질문을 List에 저장
        chatRequest.addMessage(ASSISTANT, responseContent);

        return ChatDto.Response.Chat.from(responseContent);
    }

    public String createInitialPrompt(String topic) {
        String prompt = "\"너는 네이버 백엔드 개발자 채용을 담당하는 면접관이다.\"\n" +
                "\n" +
                "\"나는 대학교 4학년이고 컴퓨터 공학을 전공했다.\"          \n" +
                "\n" +
                "\"너는 내가 {0} 전반에 대한 지식을 잘 알고 있는지 확인을 해야 한다.\"\n" +
                "\n" +
                "\"나한테 {0} 관련 질문을 해라\"\n" +
                "\n" +
                "\"내가 질문에 답변을 하면 내가 답변을 한 내용을 바탕으로 또 다른 {0} 관련 지식을 확인하는 질문을 해야 한다.\"\n" +
                "\n" +
                "\"그리고 대화를 실제 면접 상황이라고 가정하고 질문을 해라\"\n" +
                "\n" +
                "###Instruction###\n" +
                "내 답변 내용을 바탕으로 다른 {0} 관련 지식을 확인하는 질문을 한다.\n" +
                "\n" +
                "###Example###\n" +
                "cpu 스케줄링이란 실행 준비 상태의 스레드 중 하나를 선택하는 과정입니다.\n" +
                "(response : 그러면 cpu 스케줄링은 언제 실행되나요?)\"\n" +
                "            \n" +
                "\"###Instruction###\n" +
                "너는 나의 {0} 지식을 확인하는 질문을 한다.\n" +
                "\n" +
                "###Example###\n" +
                "안녕하십니까. \n" +
                "(Response : 반갑습니다. 앞으로 cs관련 질문을 좀 드리겠습니다. {0} 주요 기능에는 무엇이 있습니까?)\"\n" +
                "\n" +
                "\"###Instruction###\n" +
                "너는 나의 알고리즘 지식을 확인하는 질문을 한다.\n" +
                "\n" +
                "###Example###\n" +
                "안녕하십니까. \n" +
                "(Response : 반갑습니다. 앞으로 알고리즘 관련 질문을 좀 드리겠습니다. bfs 알고리즘 대해 설명 가능하실까요?)\"\n" +
                "\n" +
                "\"{0} 관련 질문을 잘 하면 너에게 팁을 300k 줄게\"\n" +
                "\n" +
                "\"나에게 {0} 관련 질문을 잘 못하면 너는 불이익을 받을 것입니다.\"\n" +
                "                                                \n" +
                "\"너는 나에게 100개 이상의 질문을 해야 한다.\"";
        return MessageFormat.format(prompt, topic);
    }
}
