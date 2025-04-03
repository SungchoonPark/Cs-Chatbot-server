package com.capstone.cschatbot.config.jwt.handler;

import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.common.exception.CustomException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exception")
public class JwtExceptionController {
    @GetMapping("/accessDenied")
    public void accessException() {
        throw new CustomException(CustomResponseStatus.ACCESS_DENIED);
    }

    @GetMapping("/entrypoint/nullToken")
    public void nullTokenException() {
        throw new CustomException(CustomResponseStatus.NULL_JWT);
    }

    @GetMapping("/entrypoint/expiredToken")
    public void expiredTokenException() {
       throw new CustomException(CustomResponseStatus.EXPIRED_JWT);
    }

    @GetMapping("/entrypoint/badToken")
    public void badTokenException() {
        throw new CustomException(CustomResponseStatus.BAD_JWT);
    }
}
