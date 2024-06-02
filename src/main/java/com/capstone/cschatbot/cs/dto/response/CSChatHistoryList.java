package com.capstone.cschatbot.cs.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CSChatHistoryList(List<CSChatDto> csChats) {}
