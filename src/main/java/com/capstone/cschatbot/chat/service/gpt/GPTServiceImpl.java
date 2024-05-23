package com.capstone.cschatbot.chat.service.gpt;

import com.capstone.cschatbot.chat.domain.gpt.ChatRequest;
import com.capstone.cschatbot.chat.domain.gpt.ChatResponse;
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
public class GPTServiceImpl implements GPTService {
    @Value("${openai.url}")
    private String apiUrl;

    @Qualifier("gptRestTemplate")
    @Autowired
    private final RestTemplate gptRestTemplate;

    @Override
    public String getNewQuestion(ChatRequest chatRequest) {
        ChatResponse gptResponse = gptRestTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);
        checkValidGptResponse(gptResponse);

        return gptResponse.getChoices().get(0).getMessage().getContent();
    }

    private void checkValidGptResponse(ChatResponse gptResponse) {
        if (gptResponse == null || gptResponse.getChoices() == null || gptResponse.getChoices().isEmpty()) {
            throw new CustomException(CustomResponseStatus.GPT_NOT_ANSWER);
        }
    }
}
