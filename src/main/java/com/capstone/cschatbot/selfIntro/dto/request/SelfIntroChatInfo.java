package com.capstone.cschatbot.selfIntro.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SelfIntroChatInfo(
        @NotBlank(message = "유효한 값을 입력해주세요.") String chatRoomId,
        @NotBlank(message = "유효한 값을 입력해주세요.") String clientAnswer
) {
}
