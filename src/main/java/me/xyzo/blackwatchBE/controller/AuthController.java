package me.xyzo.blackwatchBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.xyzo.blackwatchBE.dto.*;
import me.xyzo.blackwatchBE.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "인증", description = "로그인/회원가입/인증 API")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup/request")
    @Operation(summary = "회원가입 인증요청", description = "이메일로 인증코드 전송. 5분 이내 재요청 불가")
    public ResponseEntity<MessageResponseDto> signupRequest(@Valid @RequestBody SignupRequestDto request) {
        authService.sendSignupVerificationCode(request.getEmail());
        return ResponseEntity.ok(new MessageResponseDto("인증 번호가 이메일로 전송되었습니다."));
    }

    @PostMapping("/signup/verify")
    @Operation(summary = "회원가입 인증 코드 검증 + 계정 생성", description = "이메일로 수신한 코드 확인 후 계정 최종 생성")
    public ResponseEntity<MessageResponseDto> signupVerify(@Valid @RequestBody SignupVerifyDto request) {
        authService.verifySignupAndCreateAccount(request);
        return ResponseEntity.ok(new MessageResponseDto("회원가입이 완료되었습니다."));
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "ID/PW 인증 → MFA 여부에 따라 분기")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequestDto request) {
        return authService.signin(request);
    }

    @PostMapping("/mfa")
    @Operation(summary = "MFA 인증", description = "MFA 활성 사용자용 추가 인증")
    public ResponseEntity<SigninResponseDto> mfaVerify(@Valid @RequestBody MfaVerifyDto request) {
        return ResponseEntity.ok(authService.verifyMfa(request));
    }

    @GetMapping("/mfa/resend")
    @Operation(summary = "MFA 코드 재전송", description = "MFA 인증 코드 재전송")
    public ResponseEntity<MfaResponseDto> mfaResend(@RequestParam String sessionKey) {
        return ResponseEntity.ok(authService.resendMfaCode(sessionKey));
    }

    @GetMapping("/mfa/enable")
    @Operation(summary = "MFA 활성화", description = "다단계 인증 활성화")
    public ResponseEntity<MessageResponseDto> enableMfa() {
        authService.enableMfa();
        return ResponseEntity.ok(new MessageResponseDto("다단계 인증이 활성화 되었습니다."));
    }

    @GetMapping("/mfa/disable")
    @Operation(summary = "MFA 비활성화", description = "다단계 인증 비활성화 (기여자는 불가)")
    public ResponseEntity<MessageResponseDto> disableMfa() {
        authService.disableMfa();
        return ResponseEntity.ok(new MessageResponseDto("다단계 인증이 비활성화 되었습니다."));
    }

    @PostMapping("/reset-password/request")
    @Operation(summary = "비밀번호 초기화 인증 메일 발송")
    public ResponseEntity<MessageResponseDto> resetPasswordRequest(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.sendResetPasswordCode(request.getEmail());
        return ResponseEntity.ok(new MessageResponseDto("이메일로 인증번호가 전송되었습니다."));
    }

    @PostMapping("/reset-password/confirm")
    @Operation(summary = "비밀번호 초기화 사용자 입력")
    public ResponseEntity<MessageResponseDto> resetPasswordConfirm(@Valid @RequestBody ResetPasswordConfirmDto request) {
        authService.confirmResetPassword(request);
        return ResponseEntity.ok(new MessageResponseDto("비밀번호가 수정되었습니다."));
    }
}