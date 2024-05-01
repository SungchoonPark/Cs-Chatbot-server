package com.capstone.cschatbot.chat.service;

import com.capstone.cschatbot.chat.dto.ChatDto;

public interface ChatService {
    ChatDto.Response.Chat csInitChat(String memberId, String topic);

    ChatDto.Response.Chat selfInitChat(String memberId, ChatDto.Request.SelfChat chat);

    ChatDto.Response.Chat chat(String memberId, ChatDto.Request.Chat prompt);

    /** TODO: 채팅 종료기능 구현해야함 */

}
