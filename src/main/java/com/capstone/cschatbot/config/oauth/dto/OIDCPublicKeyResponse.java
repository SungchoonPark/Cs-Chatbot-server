package com.capstone.cschatbot.config.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OIDCPublicKeyResponse {
    List<OIDCPublicKeyDto> keys;
}
