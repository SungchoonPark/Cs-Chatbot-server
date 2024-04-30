package com.capstone.cschatbot.config.oauth;

import com.capstone.cschatbot.config.oauth.dto.OIDCPublicKeyResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "kakaoAuthClient",
        url = "https://kauth.kakao.com")
public interface KakaoOAuthClient {
    @Cacheable(cacheNames = "KakaoOICD", cacheManager = "oidcCacheManager")
    @GetMapping("/.well-known/jwks.json")
    OIDCPublicKeyResponse getKakaoOIDCOpenKeys();
}
