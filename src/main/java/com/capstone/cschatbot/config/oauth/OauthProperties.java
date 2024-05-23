package com.capstone.cschatbot.config.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties("oauth")
public class OauthProperties {
    private OauthSecret kakao;

    public String getKakaoAdminKey() {
        return kakao.getAdminKey();
    }

    @Getter
    @Setter
    public static class OauthSecret {
        private String baseUrl;
        private String appId;
        private String adminKey;
    }

    public String getKakaoBaseUrl() {
        return kakao.getBaseUrl();
    }

    public String getKakaoAppId() {
        return kakao.getAppId();
    }
}
