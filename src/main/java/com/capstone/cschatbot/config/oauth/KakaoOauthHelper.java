package com.capstone.cschatbot.config.oauth;

import com.capstone.cschatbot.config.oauth.dto.OIDCDecodePayload;
import com.capstone.cschatbot.config.oauth.dto.OIDCPublicKeyResponse;
import com.capstone.cschatbot.member.domain.enums.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOauthHelper {
    private final KakaoOAuthClient kakaoOAuthClient;
    private final OauthProperties oauthProperties;
    private final OauthOIDCHelper oauthOIDCHelper;

    public OIDCDecodePayload getOIDCDecodePayload(String token) {
        OIDCPublicKeyResponse oidcPublicKeyResponse = kakaoOAuthClient.getKakaoOIDCOpenKeys();

        return oauthOIDCHelper.getPayloadFromIdToken(
                token,
                oauthProperties.getKakaoBaseUrl(),
                oauthProperties.getKakaoAppId(),
                oidcPublicKeyResponse
        );
    }

    public OauthInfo getOauthInfoByToken(String idToken) {
        OIDCDecodePayload oidcDecodePayload = getOIDCDecodePayload(idToken);
        return OauthInfo.builder()
                .provider(Provider.KAKAO)
                .oid(oidcDecodePayload.getSub())
                .nickname(oidcDecodePayload.getNickname())
                .profileUrl(oidcDecodePayload.getPicture())
                .build();
    }
}
