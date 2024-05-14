package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.request.ClientAnswer;
import com.capstone.cschatbot.chat.dto.request.SelfIntroChatRequest;
import com.capstone.cschatbot.chat.dto.response.NewQuestion;

public interface ChatService {
    NewQuestion initiateCSChat(String memberId, String topic);
    NewQuestion initiateSelfIntroChat(String memberId, SelfIntroChatRequest chat);
    NewQuestion processChat(String memberId, ClientAnswer clientAnswer);
    void terminateChat(String memberId);

}
