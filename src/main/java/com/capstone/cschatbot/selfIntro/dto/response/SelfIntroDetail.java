package com.capstone.cschatbot.selfIntro.dto.response;

import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import lombok.Builder;

@Builder
public record SelfIntroDetail(SelfIntro selfIntro) {}
