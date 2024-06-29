package com.capstone.cschatbot.member.domain;

import com.capstone.cschatbot.common.model.BaseEntity;
import com.capstone.cschatbot.common.enums.BaseStatus;
import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.member.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "member")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    private String id;
    private OauthInfo oauthInfo;
    private Role role;
    private BaseStatus memberStatus;

    private Member(OauthInfo oauthInfo) {
        this.role = Role.ROLE_USER;
        this.oauthInfo = oauthInfo;
        this.memberStatus = BaseStatus.ACTIVATE;
    }

    public static Member from(OauthInfo oauthInfo) {
        return new Member(oauthInfo);
    }
}
