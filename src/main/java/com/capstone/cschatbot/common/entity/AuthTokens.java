package com.capstone.cschatbot.common.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokens {
    private String accessToken;
    private String refreshToken;
    private Long exprTime;

    public static AuthTokens of(String accessToken, String refreshToken, Long exprTime) {
        return new AuthTokens(accessToken, refreshToken, exprTime);
    }
}
