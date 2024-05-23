package com.capstone.cschatbot.member.service;

import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.member.dto.MemberDto;
import com.capstone.cschatbot.member.dto.response.Reissue;
import com.capstone.cschatbot.member.dto.response.SignIn;
import com.capstone.cschatbot.member.entity.Member;

public interface AuthService {

    SignIn login(String idToken);

    Member forceJoin(OauthInfo oauthInfo);

    Reissue reissue(String refreshToken);

    void logout(String accessToken);
}
