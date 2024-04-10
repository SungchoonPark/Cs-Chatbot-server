package com.capstone.cschatbot.config.jwt.handler;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /***
     * 해당 클래스는 Authentication 즉, 인증이 실패했을 때 거쳐가는 클래스이다.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");
        log.error("exception : " + exception);

        /**
         * 토큰 없는 경우
         */
        if (exception == null) {
            log.info("[NULL TOKEN]");
            response.sendRedirect("/exception/entrypoint/nullToken");
        }

        /**
         * 토큰 만료된 경우
         */
        if (Objects.equals(exception, CustomResponseStatus.EXPIRED_JWT.getMessage())) {
            response.sendRedirect("/exception/entrypoint/expiredToken");
        }

        /**
         * 만료를 제외한 정상적이지 않은 토큰이 들어온 경우
         */
        if (Objects.equals(exception, CustomResponseStatus.BAD_JWT.getMessage())) {
            log.info("[BAD TOKEN]");
            response.sendRedirect("/exception/entrypoint/badToken");

        }
    }
}
