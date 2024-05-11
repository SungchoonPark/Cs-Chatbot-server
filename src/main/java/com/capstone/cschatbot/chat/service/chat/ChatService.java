package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.ChatResponse;
import com.capstone.cschatbot.chat.dto.response.EvaluationAndQuestionResponse;

public interface ChatService {
    ChatResponse initiateCSChat(String memberId, String topic);
    ChatResponse initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat);
    EvaluationAndQuestionResponse processChat(String memberId, ClientAnswer clientAnswer);
    ChatResponse testProcessChat(String memberId, ClientAnswer clientAnswer);
    void terminateChat(String memberId);

}
