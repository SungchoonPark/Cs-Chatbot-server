package com.capstone.cschatbot.selfIntro.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SelfIntroList(
        List<SelfIntroDto> selfIntros) {
}
