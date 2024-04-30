package com.capstone.cschatbot.config.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OIDCDecodePayload {
    /** issuer ex) https://kauth.kakao.com */
    private String iss;
    /** client id */
    private String aud;
    /** oauth provider account unique id */
    private String sub;
    /** client email */
    private String email;
    /** client nickname */
    private String nickname;
    /** client profile picture */
    private String picture;
}
