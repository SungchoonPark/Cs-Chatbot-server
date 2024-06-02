package com.capstone.cschatbot.selfIntro.dto.response;

import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
public record SelfIntroList(
        List<SelfIntroDto> selfIntros) {
}
