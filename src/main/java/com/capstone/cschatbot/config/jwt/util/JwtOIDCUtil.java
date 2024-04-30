package com.capstone.cschatbot.config.jwt.util;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import com.capstone.cschatbot.config.oauth.dto.OIDCDecodePayload;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Service
@Getter
@Slf4j
public class JwtOIDCUtil {
    private final String KID = "kid";

    /**
     * 미인증 토큰에서 Kid를 가져옴
     */
    public String getKidFromUnsignedTokenHeader(String token, String iss, String aud) {
        return (String) getUnsignedTokenClaims(token, iss, aud).getHeader().get(KID);
    }

    private Jwt<Header, Claims> getUnsignedTokenClaims(String token, String iss, String aud) {
        try {
            return Jwts.parserBuilder()
                    .requireAudience(aud) // aud(내 앱 id) 가 같은지 확인
                    .requireIssuer(iss) // issuer가 kakao인지 확인
                    .build()
                    .parseClaimsJwt(getUnsignedToken(token));
        } catch (ExpiredJwtException e) {
            throw new CustomException(CustomResponseStatus.EXPIRED_JWT);
        } catch (Exception e) {
            throw new CustomException(CustomResponseStatus.BAD_JWT);
        }
    }

    public Jws<Claims> getOIDCTokenJws(String token, String modulus, String exponent) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getRSAPublicKey(modulus, exponent))
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(CustomResponseStatus.EXPIRED_JWT);
        } catch (Exception e) {
            throw new CustomException(CustomResponseStatus.BAD_JWT);
        }
    }

    private String getUnsignedToken(String token) {
        String[] splitToken = token.split("\\.");
        if (splitToken.length != 3) throw new CustomException(CustomResponseStatus.BAD_TOKEN);
        return splitToken[0] + "." + splitToken[1] + ".";
    }

    /** OIDC Body에 들어있는 정보들을 빼옴 */
    public OIDCDecodePayload getOIDCTokenBody(String token, String modulus, String exponent) {
        Claims body = getOIDCTokenJws(token, modulus, exponent).getBody();
        return new OIDCDecodePayload(
                body.getIssuer(),
                body.getAudience(),
                body.getSubject(),
                body.get("email", String.class),
                body.get("picture", String.class),
                body.get("nickname", String.class));
    }

    private Key getRSAPublicKey(String modulus, String exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
        byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
        BigInteger n = new BigInteger(1, decodeN);
        BigInteger e = new BigInteger(1, decodeE);

        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(n, e);
        return keyFactory.generatePublic(rsaPublicKeySpec);
    }
}
