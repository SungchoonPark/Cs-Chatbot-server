package com.capstone.cschatbot.member.dto.response;

import com.capstone.cschatbot.common.entity.AuthTokens;
import lombok.Builder;

@Builder
public record Reissue(AuthTokens authTokens) {}
