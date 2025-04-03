package com.capstone.cschatbot.member.presentation;

import com.capstone.cschatbot.common.dto.ApiResponse;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.member.dto.response.Reissue;
import com.capstone.cschatbot.member.dto.response.SignIn;
import com.capstone.cschatbot.member.application.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {
    private final AuthService authService;

    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<SignIn>> login(@RequestParam String idToken) {
        SignIn response = authService.login(idToken);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Reissue>> reissue(@RequestHeader("Authorization") String refreshToken) {
        Reissue response = authService.reissue(refreshToken);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PostMapping("/member/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok().body(ApiResponse.createSuccess("Logout Success", CustomResponseStatus.SUCCESS));
    }
}
