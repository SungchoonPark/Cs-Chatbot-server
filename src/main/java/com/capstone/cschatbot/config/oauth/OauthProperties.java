package com.capstone.cschatbot.config.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@RequiredArgsConstructor
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
//        private String clientId;
//        private String clientSecret;
//        private String redirectUrl;
        private String appId;
        private String adminKey;
    }

    public String getKakaoBaseUrl() {
        return kakao.getBaseUrl();
    }

//    public String getKakaoClientId() {
//        return kakao.getClientId();
//    }
//
//    public String getKakaoClientSecret() {
//        return kakao.getClientSecret();
//    }
//
//    public String getKakaoRedirectUrl() {
//        return kakao.getRedirectUrl();
//    }

    public String getKakaoAppId() {
        return kakao.getAppId();
    }
}
