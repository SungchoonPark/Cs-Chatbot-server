package com.capstone.cschatbot.member.dto.response;

import com.capstone.cschatbot.common.model.AuthTokens;
import lombok.Builder;

@Builder
public record Reissue(AuthTokens authTokens) {}
