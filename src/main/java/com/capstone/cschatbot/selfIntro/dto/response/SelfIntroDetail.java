package com.capstone.cschatbot.selfIntro.dto.response;

import com.capstone.cschatbot.selfIntro.dto.SelfIntroChat;
import lombok.Builder;

import java.util.List;

@Builder
public record SelfIntroDetail(List<SelfIntroChat> selfIntroChats) {}
