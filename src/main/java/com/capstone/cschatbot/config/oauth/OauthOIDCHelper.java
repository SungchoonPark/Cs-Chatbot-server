package com.capstone.cschatbot.config.oauth;


import com.capstone.cschatbot.config.jwt.util.JwtOIDCUtil;
import com.capstone.cschatbot.config.oauth.dto.OIDCDecodePayload;
import com.capstone.cschatbot.config.oauth.dto.OIDCPublicKeyDto;
import com.capstone.cschatbot.config.oauth.dto.OIDCPublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OauthOIDCHelper {
    private final JwtOIDCUtil jwtOIDCUtil;

    private String getKidFromUnsignedIdToken(String token, String iss, String aud) {
        return jwtOIDCUtil.getKidFromUnsignedTokenHeader(token, iss, aud);
    }

    public OIDCDecodePayload getPayloadFromIdToken(
            String token, String iss, String aud, OIDCPublicKeyResponse oidcPublicKeyResponse) {
        String kid = getKidFromUnsignedIdToken(token, iss, aud);

        OIDCPublicKeyDto oidcPublicKeyDto = oidcPublicKeyResponse.getKeys().stream()
                .filter(o -> o.getKid().equals(kid))
                .findFirst()
                .orElseThrow();

        return jwtOIDCUtil.getOIDCTokenBody(token, oidcPublicKeyDto.getN(), oidcPublicKeyDto.getE());
    }

}
