package com.capstone.cschatbot.chat.service.chat;

import com.capstone.cschatbot.chat.dto.ChatDto;

public interface ChatService {
    ChatDto.Response.Chat initiateCSChat(String memberId, String topic);
    ChatDto.Response.Chat initiateSelfIntroChat(String memberId, ChatDto.Request.SelfIntroChat chat);
    ChatDto.Response.EvaluationAndQuestion processChat(String memberId, ChatDto.Request.Chat prompt);
    void terminateChat(String memberId);

}
