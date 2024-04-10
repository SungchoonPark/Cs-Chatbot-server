package com.capstone.cschatbot.common.exception;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {
    private final CustomResponseStatus customResponseStatus;
}
