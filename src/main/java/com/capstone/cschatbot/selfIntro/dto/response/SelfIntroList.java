package com.capstone.cschatbot.selfIntro.dto.response;

import com.capstone.cschatbot.selfIntro.entity.SelfIntro;
import lombok.Builder;

import java.util.List;

@Builder
public record SelfIntroList(List<SelfIntro> selfIntros) {}
