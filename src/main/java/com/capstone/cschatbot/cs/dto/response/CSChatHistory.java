package com.capstone.cschatbot.cs.dto.response;

import com.capstone.cschatbot.cs.entity.CSChat;
import lombok.Builder;

@Builder
public record CSChatHistory(CSChat csChat) {}
