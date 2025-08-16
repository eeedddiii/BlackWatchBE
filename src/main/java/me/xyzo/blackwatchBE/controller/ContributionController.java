package me.xyzo.blackwatchBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.xyzo.blackwatchBE.dto.*;
import me.xyzo.blackwatchBE.service.ContributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contrib")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "기여자 기능", description = "기여자 신청 및 관리 API")
public class ContributionController {

    @Autowired
    private ContributionService contributionService;

    @PostMapping("/applications")
    @Operation(summary = "기여자 신청: FORM 제출", description = "기여자가 되서 크롤링 데이터를 제출하려면 먼저 기여자 권한을 받아야 한다")
    public ResponseEntity<ContributionApplicationResponseDto> submitApplication(@RequestBody ContributionApplicationDto request) {
        ContributionApplicationResponseDto response = contributionService.submitApplication(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications/me")
    @Operation(summary = "기여자 신청: 상태확인", description = "내 신청 현황을 확인할 수 있다")
    public ResponseEntity<ContributionApplicationStatusDto> getMyApplicationStatus() {
        return ResponseEntity.ok(contributionService.getMyApplicationStatus());
    }

    @PostMapping("/secret")
    @Operation(summary = "Client Secret (재)발급", description = "새 Client Secret을 발급하며 이전 비밀키는 즉시 폐기")
    public ResponseEntity<ClientSecretResponseDto> generateClientSecret() {
        ClientSecretResponseDto response = contributionService.generateClientSecret();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "정보조회", description = "내 기여자 상태 및 통합 정보")
    public ResponseEntity<ContributorInfoDto> getMyContributorInfo() {
        return ResponseEntity.ok(contributionService.getMyContributorInfo());
    }

    @GetMapping("/sessions")
    @Operation(summary = "세션 조회", description = "MongoDB에서 ClientID를 조회해 세션 데이터 호출")
    public ResponseEntity<ContributorSessionDto> getSessions() {
        return ResponseEntity.ok(contributionService.getSessions());
    }

    @DeleteMapping("/sessions")
    @Operation(summary = "세션 제거", description = "MongoDB에서 ClientID를 조회해 해당 세션 제거")
    public ResponseEntity<MessageResponseDto> deleteSessions() {
        contributionService.deleteSessions();
        return ResponseEntity.ok(new MessageResponseDto("세션이 제거되었습니다."));
    }
}