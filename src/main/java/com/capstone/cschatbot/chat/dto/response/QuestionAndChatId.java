package com.capstone.cschatbot.chat.dto.response;

import lombok.Builder;

@Builder
public record QuestionAndChatId(String question, String chatRoomId) {}
