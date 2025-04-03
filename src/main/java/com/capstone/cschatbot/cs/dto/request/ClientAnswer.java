package com.capstone.cschatbot.cs.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ClientAnswer(@NotBlank(message = "유효한 값을 입력해주세요.") String answer) {

}
