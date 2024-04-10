package com.capstone.cschatbot.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum CustomResponseStatus {

    /***
     * 1000: 요청 성공
     */
    SUCCESS(HttpStatusCode.valueOf(HttpStatus.OK.value()), "1000", "요청에 성공하였습니다."),

    /***
     * 2000: UNAUTHORIZED
     */
    EXPIRED_JWT(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), "2000", "만료된 토큰입니다."),
    BAD_JWT(HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value()), "2001", "잘못된 토큰입니다."),

    /***
     * 3000: ACCESS DENIED
     */
    ACCESS_DENIED(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "3000", "인증되지 않은 사용자입니다."),
    LOGOUT_MEMBER(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()), "3001", "로그아웃된 사용자입니다."),

    /***
     * 4000: NOT_FOUND
     */
    NULL_JWT(HttpStatusCode.valueOf(HttpStatus.NO_CONTENT.value()), "4000", "토큰이 공백입니다."),
    MEMBER_NOT_FOUND(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "4001", "해당 유저를 찾을 수 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "4002", "리프레시 토큰을 찾을 수 없습니다."),
    ROLE_NOT_FOUND(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()), "4003", "해당 권한을 찾을 수 없습니다."),

    /***
     * 5000: NOT_MATCH
     */
    REFRESH_TOKEN_NOT_MATCH(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), "5000", "잘못된 리프레시 토큰입니다."),

    /***
     * 6000: Internal_Server_Error
     */
    INTERNAL_SERVER_ERROR(HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), "6000", "내부 서버 오류입니다.");


    private final HttpStatusCode httpStatusCode;
    private final String code;
    private final String message;

    CustomResponseStatus(HttpStatusCode httpStatusCode, String code, String message) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.message = message;
    }
}
