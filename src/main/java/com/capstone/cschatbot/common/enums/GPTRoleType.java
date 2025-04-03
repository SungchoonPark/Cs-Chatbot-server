package com.capstone.cschatbot.common.enums;

import lombok.Getter;

@Getter
public enum GPTRoleType {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private String role;

    GPTRoleType(String role) {
        this.role = role;
    }
}
