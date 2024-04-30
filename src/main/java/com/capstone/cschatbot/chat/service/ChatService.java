package com.capstone.cschatbot.chat.service;

import com.capstone.cschatbot.chat.dto.ChatDto;

public interface ChatService {
    ChatDto.Response.Chat initialChat(String topic);

    ChatDto.Response.Chat chat(ChatDto.Request.Chat prompt);

    ChatDto.Response.Chat selfInitChat(ChatDto.Self_Request.Chat chat);
    ChatDto.Response.Chat selfchat(ChatDto.Request.Chat prompt);

}
