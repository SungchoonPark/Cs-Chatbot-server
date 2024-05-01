package com.capstone.cschatbot.member.controller;

import com.capstone.cschatbot.common.dto.ApiResponse;
import com.capstone.cschatbot.common.enums.CustomResponseStatus;
import com.capstone.cschatbot.member.dto.MemberDto;
import com.capstone.cschatbot.member.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {
    private final AuthService authService;

    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResponse<MemberDto.Response.SignIn>> login(@RequestParam String idToken) {
        MemberDto.Response.SignIn response = authService.login(idToken);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<MemberDto.Response.Reissue>> reissue(@RequestHeader("Authorization") String refreshToken) {
        MemberDto.Response.Reissue response = authService.reissue(refreshToken);
        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok().body(ApiResponse.createSuccess("Logout Success", CustomResponseStatus.SUCCESS));
    }
}
