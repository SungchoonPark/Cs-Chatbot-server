package com.capstone.cschatbot.cs.dto.response;

import com.capstone.cschatbot.cs.domain.ChatEvaluation;
import lombok.Builder;

import java.util.List;

@Builder
public record CSChatHistory(List<ChatEvaluation> chatEvaluations) {}
