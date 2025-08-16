package me.xyzo.blackwatchBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.xyzo.blackwatchBE.dto.AccountUpdateDto;
import me.xyzo.blackwatchBE.dto.UserProfileDto;
import me.xyzo.blackwatchBE.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "계정정보 기능", description = "사용자 계정 정보 관리 API")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/me")
    @Operation(summary = "내 계정 정보 조회")
    public ResponseEntity<UserProfileDto> getMyAccount() {
        return ResponseEntity.ok(accountService.getMyAccount());
    }

    @PatchMapping("/me")
    @Operation(summary = "내 계정 정보 수정")
    public ResponseEntity<UserProfileDto> updateMyAccount(@RequestBody AccountUpdateDto request) {
        return ResponseEntity.ok(accountService.updateMyAccount(request));
    }
}