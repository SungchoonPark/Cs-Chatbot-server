package com.capstone.cschatbot.common.dto;

import lombok.Builder;

@Builder
public record QuestionAndChatId(String question, String chatRoomId) {}
