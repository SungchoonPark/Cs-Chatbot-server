package com.capstone.cschatbot.member.service;

import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.member.dto.MemberDto;
import com.capstone.cschatbot.member.entity.Member;

public interface AuthService {

    MemberDto.Response.SignIn login(String idToken);

    Member forceJoin(OauthInfo oauthInfo);

    MemberDto.Response.Reissue reissue(String refreshToken);
}
