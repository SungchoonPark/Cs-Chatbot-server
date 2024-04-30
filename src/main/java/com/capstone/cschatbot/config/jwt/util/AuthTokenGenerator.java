package com.capstone.cschatbot.config.jwt.util;

import com.capstone.cschatbot.common.entity.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenGenerator {
    private final JwtUtil jwtUtil;

    public AuthTokens generate(String id) {
        String accessToken = jwtUtil.createToken(id, TokenType.ACCESS_TOKEN);
        String refreshToken = jwtUtil.createToken(id, TokenType.REFRESH_TOKEN);

        return AuthTokens.of(accessToken, refreshToken, jwtUtil.getExpiration(TokenType.ACCESS_TOKEN));
    }

    public AuthTokens generate(String id, String refreshToken) {
        String accessToken = jwtUtil.createToken(id, TokenType.ACCESS_TOKEN);

        return AuthTokens.of(accessToken, refreshToken, jwtUtil.getExpiration(TokenType.ACCESS_TOKEN));
    }
}
