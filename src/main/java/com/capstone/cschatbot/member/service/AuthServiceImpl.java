package com.capstone.cschatbot.member.service;

import com.capstone.cschatbot.common.entity.AuthTokens;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.config.jwt.util.AuthTokenGenerator;
import com.capstone.cschatbot.config.jwt.util.JwtUtil;
import com.capstone.cschatbot.config.jwt.util.TokenType;
import com.capstone.cschatbot.config.oauth.KakaoOauthHelper;
import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.config.redis.util.RedisUtil;
import com.capstone.cschatbot.member.dto.MemberDto;
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
public class AuthServiceImpl implements AuthService{
    private final MemberRepository memberRepository;
    private final KakaoOauthHelper kakaoOauthHelper;
    private final AuthTokenGenerator authTokenGenerator;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    private static final String RT = "RT:";

    @Override
    public MemberDto.Response.SignIn login(String idToken) {
        OauthInfo oauthInfo = kakaoOauthHelper.getOauthInfoByToken(idToken);

        Member findMember = memberRepository.findByOauthInfoOid(oauthInfo.getOid())
                .orElseGet(() -> forceJoin(oauthInfo));

        String refreshToken = redisUtil.getData(RT + findMember.getId());
        if (refreshToken == null) {
            refreshToken = jwtUtil.createToken(findMember.getId().toString(), TokenType.REFRESH_TOKEN);
            redisUtil.setData(RT + findMember.getId(), refreshToken, jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));
        }

        return MemberDto.Response.SignIn.of(
                authTokenGenerator.generate(findMember.getId().toString(), refreshToken),
                findMember.getOauthInfo().getNickname());
    }

    @Override
    public Member forceJoin(OauthInfo oauthInfo) {
        Member newMember = Member.from(oauthInfo);
        return memberRepository.save(newMember);
    }

    @Override
    public MemberDto.Response.Reissue reissue(String refreshToken) {
        String resolveToken = jwtUtil.resolveToken(refreshToken);
        String idInToken = jwtUtil.getIdInToken(resolveToken);

        String refreshTokenInRedis = redisUtil.getData(RT + idInToken);

        if (!Objects.equals(resolveToken, refreshTokenInRedis)) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_MATCH);
        }

        AuthTokens generate = authTokenGenerator.generate(idInToken);
        redisUtil.setData(RT + idInToken, generate.getRefreshToken(), jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));

        return MemberDto.Response.Reissue.from(generate);
    }
}
