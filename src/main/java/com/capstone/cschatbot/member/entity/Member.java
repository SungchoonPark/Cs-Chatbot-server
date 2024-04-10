package com.capstone.cschatbot.member.entity;

import com.capstone.cschatbot.common.entity.BaseEntity;
import com.capstone.cschatbot.common.enums.BaseStatus;
import com.capstone.cschatbot.member.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private BaseStatus memberStatus;
}
