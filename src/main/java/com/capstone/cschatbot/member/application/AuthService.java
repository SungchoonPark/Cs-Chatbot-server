package com.capstone.cschatbot.member.application;

import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.member.dto.response.Reissue;
import com.capstone.cschatbot.member.dto.response.SignIn;
import com.capstone.cschatbot.member.domain.Member;

public interface AuthService {

    SignIn login(String idToken);

    Member forceJoin(OauthInfo oauthInfo);

    Reissue reissue(String refreshToken);

    void logout(String accessToken);
}
