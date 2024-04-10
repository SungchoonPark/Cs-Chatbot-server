package com.capstone.cschatbot.common.dto;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ApiResponse<T> {
    private HttpStatusCode httpStatusCode;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> createSuccess(T data, CustomResponseStatus customResponseStatus) {
        return new ApiResponse<>(
                HttpStatusCode.valueOf(HttpStatus.OK.value()),
                customResponseStatus.getCode(),
                customResponseStatus.getMessage(),
                data);
    }

    /***
     * @param bindingResult : @Valid 의 유효성 검사를 실패한 값(필드)들
     * @return : HttpStatus 와 Code, Message, 오류 데이터를 반환한다.
     */
    public static ApiResponse<Map<String, String>> createNotValid(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (ObjectError error : allErrors) {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        }

        return new ApiResponse<>(
                HttpStatusCode.valueOf(HttpStatus.BAD_REQUEST.value()),
                "NOT_VALID",
                "유효하지 않은 데이터입니다.",
                errors
        );
    }

    /***
     * 예외처리를 위한 메서드
     * @param status : Custom Status Code
     * @return : data 없이 ApiResponse 를 반환한다.
     */
    public static ApiResponse<String> createError(CustomResponseStatus status) {
        return new ApiResponse<>(status.getHttpStatusCode(), status.getMessage(), status.getCode(), null);

    }
}
