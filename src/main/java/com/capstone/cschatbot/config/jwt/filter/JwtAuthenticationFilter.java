package com.capstone.cschatbot.config.jwt.filter;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.config.jwt.util.JwtUtil;
import com.capstone.cschatbot.config.redis.util.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String EXCEPTION = "exception";
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtUtil.resolveToken(request.getHeader("Authorization"));

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (request.getRequestURI().equals("/api/v1/auth/reissue")) {
                filterChain.doFilter(request, response);
                return;
            }

            String isLogout = redisUtil.getData("LOGOUT:"+token);
            // getData 해서 값이 가져와지면 AT가 블랙리스트에 등록된 상태이므로 로그아웃된 상태임.
            if (isLogout != null) {
                request.setAttribute(EXCEPTION, CustomResponseStatus.LOGOUT_MEMBER.getMessage());
                return;
            }

            Authentication authentication = jwtUtil.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {
            log.error("Enter [EXPIRED TOKEN]");
            request.setAttribute(EXCEPTION, CustomResponseStatus.EXPIRED_JWT.getMessage());
        } catch (JwtException | IllegalArgumentException | SignatureException
                 | UnsupportedJwtException | MalformedJwtException e) {
            log.error("Enter [INVALID TOKEN]");
            request.setAttribute(EXCEPTION, CustomResponseStatus.BAD_JWT.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/api/v1/sign-in"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
