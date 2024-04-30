package com.capstone.cschatbot.member.entity;

import com.capstone.cschatbot.common.entity.BaseEntity;
import com.capstone.cschatbot.common.enums.BaseStatus;
import com.capstone.cschatbot.config.oauth.OauthInfo;
import com.capstone.cschatbot.member.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private OauthInfo oauthInfo;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
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
