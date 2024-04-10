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

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private static final String USER = "user";
    private static final String ASSISTANT = "assistant";

    @Value("${openai.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Qualifier("chatRestTemplate")
    @Autowired
    private final RestTemplate restTemplate;

    private ChatRequest chatRequest;

    @Override
    public ChatDto.Response.Chat initialChat() {
        chatRequest = new ChatRequest(model);
        chatRequest.addMessage(ASSISTANT, createInitialPrompt());

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

    public String createInitialPrompt() {
        return "이제부터 너는 네이버 백엔드 개발자 채용을 담당하는 면접관이다.\n" +
                "너는 내가 운영체제 전반에 대한 지식을 잘 알고 있는지 확인을 하고 싶어한다.\n" +
                "나한테 운영체제 관련 질문을 해라\n" +
                "그리고 내가 질문에 답변을 하면 내가 답변을 한 내용을 바탕으로 또 다른 질문을 해라\n" +
                "그리고 대화를 실제 면접 상황이라고 가정하고 질문을 해라\n" +
                "그리고 질문을 할 때 '1. 운영체는 무엇인가요?' 이런 식으로 숫자를 쓰지 말고 실제 면접관 말투로 질문해라.\n" +
                "내가 모른다고 하면 설명해줄 필요는 없고 다음 질문으로 넘어가라\n" +
                "이 면접은 적어도 100개의 운영체제 관련 질문을 한 후에 종료한다.\n" +
                "그리고 내가 종료라고 하면 이 면접은 끝난다.";
    }
}
