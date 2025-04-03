package com.capstone.cschatbot.selfIntro.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SelfIntroChatRequest(
        @NotBlank(message = "유효한 값을 입력해주세요.") String question,
        @NotBlank(message = "유효한 값을 입력해주세요.") String content) {

}
