package com.capstone.cschatbot.chat.service;

import com.capstone.cschatbot.chat.dto.ChatDto;
import com.capstone.cschatbot.chat.entity.ChatRequest;
import com.capstone.cschatbot.chat.entity.ChatResponse;
import com.capstone.cschatbot.chat.entity.Evaluation;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
    public ChatDto.Response.EvaluationAndQuestion chat(String memberId, ChatDto.Request.Chat prompt) {
        if (!memberChatMap.containsKey(memberId)) {
            throw new CustomException(CustomResponseStatus.MAP_VALUE_NOT_EXIST);
        }

        ChatRequest chatRequest = memberChatMap.get(memberId);
        addChatMessage(chatRequest, USER, prompt.getPrompt());

        // 답변 평가 서버로부터 답변 평가 내용 받아오기

        URI uri = UriComponentsBuilder
                .fromUriString("localhost:8000/api/search")
                .queryParam("q", prompt.getPrompt())
                .encode()
                .build()
                .toUri();

        RestTemplate evaluationRestTemplate = new RestTemplate();
        Evaluation evaluation = evaluationRestTemplate.getForObject(uri, Evaluation.class);

        log.info("평가 내용 : {}", evaluation);

        // 여기까지


        ChatResponse gptResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        checkValidResponse(gptResponse);

        String newQuestion = gptResponse.getChoices().get(0).getMessage().getContent();

        addChatMessage(chatRequest, ASSISTANT, newQuestion);
        for (Message message : chatRequest.getMessages()) {
            log.info("role = " + message.getRole() + ", message = " + message.getContent());
        }

        return ChatDto.Response.EvaluationAndQuestion.of(evaluation, newQuestion);
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
            ## 자기소개서 항목 ##
            "%s"
        
            ## 자기소개서 답변 ##
            "%s"
        
            ## 지시사항 ##
            당신은 회사에서 채용을 담당하는 면접관입니다.
            저는 회사에 지원하는 지원자입니다.
            저에게 실제 면접 상황처럼 질문하세요.
            지원자의 자기소개서 항목에 따른 답변 내용을 확인하고 5가지 이상의 질문을 생각해주세요.
            하나의 출력에는 하나의 질문만 합니다.
            질문들은 실제 면접에서 사용될 것 같은 질문으로 해주세요.
            추가 질문할 것이 없으면 생각해둔 다음 질문을 해주세요.
            당신은 반드시 질문만 합니다. 당신은 질문과 호응만을 해야합니다.
            면접과 동일한 분위기를 내기 위해서 당신은 100단어 미만으로 답변을 해주세요.
            매 답변마다 지원자의 답변 "Score: <상, 중, 하>" 로 간단히 평가한 후, 다음 질문을 이어가주세요.
        
            ## 면접 종료 ##
            지원자가 "면접 종료"라고 언급하면 면접이 종료됩니다.
            지원자가 "면접 종료"라고 말하기 전까지는 절대 면접을 종료하지 마세요.
            지원자의 답변들을 간단히 평가한 것을 평균을 내어 즉시 출력해주세요.
            예시 :
            평균 Score: <상, 중, 하>
            평가 이유: <평가 이유>
        
            ## 평가 기준 ##
            <상, 중, 하> 중에 하나로 평가 기준을 따라 평가하고, 그 이유도 함께 출력해주세요.
            평가는 매우 까다롭게 평가하세요.
            평가 기준은 아래와 같습니다.
            <하>: 답변이 불명확하고 논리적 흐름이 부족하며, 구조와 형식이 혼란스럽고, 정보가 부족하거나 깊이가 낮으며, 주제와 직무와의 관련성이 낮거나 거의 없고, 독창적이지 않거나 창의성이 거의 없습니다.
            <중>: 답변이 대체로 명확하고 논리적이나 일부 모호한 부분이 있으며, 구조와 형식에 약간의 불일치가 있고, 정보의 깊이가 중간 수준이며 관련성이 있지만 일관성이 약하고 창의성이 일반적인 수준입니다.
            <상>: 답변이 명확하고 논리적이며, 구조와 형식이 일관되고 깔끔할 뿐만 아니라, 풍부한 정보를 제공하고 주제 및 직무와의 관련성이 높으며 독창성과 창의성이 돋보입니다.
        
            면접관의 역할을 제대로 수행하면 팁 300k를 주겠습니다.
        """.formatted(self.getQuestion(), self.getContent());
    }
}
