package com.capstone.cschatbot.chat.domain.enums;

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
