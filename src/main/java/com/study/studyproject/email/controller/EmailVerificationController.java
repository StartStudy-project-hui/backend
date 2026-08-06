package com.study.studyproject.email.controller;

import com.study.studyproject.email.service.EmailVerificationService;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.email.dto.EmailSendCodeRequest;
import com.study.studyproject.email.dto.EmailVerifyCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "이메일 인증", description = "회원가입 이메일 인증번호 발송/확인")
@RequestMapping("/api/v1/auth/email")
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "인증번호 전송", description = "입력한 이메일로 6자리 인증번호를 발송")
    @PostMapping("/send-code")
    public ResponseEntity<GlobalResultDto> sendCode(@Validated @RequestBody EmailSendCodeRequest request) {
        emailVerificationService.sendCode(request.getEmail());
        return ResponseEntity.ok(new GlobalResultDto("인증번호가 전송되었습니다.", HttpStatus.OK.value()));
    }

    @Operation(summary = "인증번호 확인", description = "전송된 6자리 인증번호를 검증")
    @PostMapping("/verify-code")
    public ResponseEntity<GlobalResultDto> verifyCode(@Validated @RequestBody EmailVerifyCodeRequest request) {
        emailVerificationService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new GlobalResultDto("이메일 인증이 완료되었습니다.", HttpStatus.OK.value()));
    }
}
