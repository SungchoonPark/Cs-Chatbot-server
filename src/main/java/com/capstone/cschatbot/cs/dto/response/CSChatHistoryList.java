package com.capstone.cschatbot.cs.dto.response;

import com.capstone.cschatbot.cs.domain.CSChat;
import lombok.Builder;

import java.util.List;

@Builder
public record CSChatHistoryList(List<CSChat> csChats) {}
