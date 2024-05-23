package com.capstone.cschatbot.member.service;

import com.capstone.cschatbot.common.domain.AuthTokens;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.config.jwt.util.AuthTokenGenerator;
import com.capstone.cschatbot.config.jwt.util.JwtUtil;
import com.capstone.cschatbot.config.jwt.util.TokenType;
import com.capstone.cschatbot.config.oauth.KakaoOauthHelper;
import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.config.redis.util.RedisUtil;
import com.capstone.cschatbot.member.dto.response.Reissue;
import com.capstone.cschatbot.member.dto.response.SignIn;
import com.capstone.cschatbot.member.entity.Member;
import com.capstone.cschatbot.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final KakaoOauthHelper kakaoOauthHelper;
    private final AuthTokenGenerator authTokenGenerator;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;
    private static final String RT = "RT:";
    private static final String LOGOUT = "LOGOUT:";

    @Override
    public SignIn login(String idToken) {
        Member findMember = findByMemberByOauthInfo(kakaoOauthHelper.getOauthInfoByToken(idToken));

        return SignIn.builder()
                .authTokens(
                        authTokenGenerator.generate(
                                findMember.getId(),
                                getOrGenerateRefreshToken(findMember)
                        )
                )
                .nickname(findMember.getOauthInfo().getNickname())
                .build();
    }

    @Override
    public Member forceJoin(OauthInfo oauthInfo) {
        Member newMember = Member.from(oauthInfo);
        return memberRepository.save(newMember);
    }

    @Override
    public Reissue reissue(String refreshToken) {
        String idInRefreshToken = getIdInRefreshToken(refreshToken);

        AuthTokens authTokens = authTokenGenerator.generate(idInRefreshToken);
        redisUtil.setData(RT + idInRefreshToken, authTokens.getRefreshToken(), jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));

        return Reissue.builder()
                .authTokens(authTokens)
                .build();
    }

    @Override
    public void logout(String accessToken) {
        String resolveAccessToken = jwtUtil.resolveToken(accessToken);
        String idInToken = jwtUtil.getIdInToken(resolveAccessToken);
        String refreshTokenInRedis = redisUtil.getData(RT + idInToken);
        checkValidRefreshTokenInRedis(refreshTokenInRedis);

        redisUtil.deleteDate(RT + idInToken);
        redisUtil.setData(LOGOUT, resolveAccessToken, jwtUtil.getExpiration(resolveAccessToken));
    }

    private String getIdInRefreshToken(String refreshToken) {
        String resolveRefreshToken = jwtUtil.resolveToken(refreshToken);
        String idInToken = jwtUtil.getIdInToken(resolveRefreshToken);
        String refreshTokenInRedis = redisUtil.getData(RT + idInToken);

        if (!Objects.equals(resolveRefreshToken, refreshTokenInRedis)) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_MATCH);
        }
        return idInToken;
    }

    private String getOrGenerateRefreshToken(Member findMember) {
        String refreshToken = redisUtil.getData(RT + findMember.getId());

        if (refreshToken == null) {
            refreshToken = jwtUtil.createToken(findMember.getId(), TokenType.REFRESH_TOKEN);
            redisUtil.setData(RT + findMember.getId(), refreshToken, jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));
        }

        return refreshToken;
    }

    private Member findByMemberByOauthInfo(OauthInfo oauthInfo) {
        return memberRepository.findByOauthInfoOid(oauthInfo.getOid())
                .orElseGet(() -> forceJoin(oauthInfo));
    }

    private static void checkValidRefreshTokenInRedis(String refreshTokenInRedis) {
        if (refreshTokenInRedis == null) throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND);
    }
}
