package com.capstone.cschatbot.config.oauth;

import com.capstone.cschatbot.member.domain.enums.Provider;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {
    private Provider provider;
    private String oid;
    private String nickname;
    private String profileUrl;

    @Builder
    public OauthInfo(Provider provider, String oid, String nickname, String profileUrl) {
        this.provider = provider;
        this.oid = oid;
        this.nickname = nickname;
        this.profileUrl = profileUrl;
    }
}
