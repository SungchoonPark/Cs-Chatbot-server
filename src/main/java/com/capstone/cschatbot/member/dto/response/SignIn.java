package com.capstone.cschatbot.member.dto.response;

import com.capstone.cschatbot.common.domain.AuthTokens;
import lombok.Builder;

@Builder
public record SignIn(AuthTokens authTokens, String nickname) {}
