package me.xyzo.blackwatchBE.service;

import com.github.f4b6a3.ulid.UlidCreator;
import me.xyzo.blackwatchBE.domain.ClientSecret;
import me.xyzo.blackwatchBE.domain.ContributionApplication;
import me.xyzo.blackwatchBE.domain.User;
import me.xyzo.blackwatchBE.dto.*;
import me.xyzo.blackwatchBE.exception.BadRequestException;
import me.xyzo.blackwatchBE.exception.ForbiddenException;
import me.xyzo.blackwatchBE.exception.NotFoundException;
import me.xyzo.blackwatchBE.repository.ClientSecretRepository;
import me.xyzo.blackwatchBE.repository.ContributionApplicationRepository;
import me.xyzo.blackwatchBE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ContributionService {

    @Autowired
    private ContributionApplicationRepository applicationRepository;

    @Autowired
    private ClientSecretRepository clientSecretRepository;

    @Autowired
    private UserRepository userRepository;

    public ContributionApplicationResponseDto submitApplication(ContributionApplicationDto request) {
        String userId = getCurrentUserId();

        // 이미 신청한 경우 체크
        if (applicationRepository.existsById(userId)) {
            throw new BadRequestException("이미 기여자 신청을 하셨습니다.");
        }

        ContributionApplication application = new ContributionApplication();
        application.setUserId(userId);
        application.setContact(request.getContact());
        application.setHandle(request.getHandle());
        application.setJobs(request.getJobs());
        application.setMotivation(request.getMotivation());
        application.setLaw(request.isLaw());
        application.setLicense(request.isLicense());
        application.setStatus(ContributionApplication.Status.TEMPORARY_ACCEPT);

        // Client ID 생성 (임시 승인된 기여자)
        application.setClientId(UUID.randomUUID().toString());

        applicationRepository.save(application);

        return new ContributionApplicationResponseDto(
                userId,
                "Temporary Accept",
                application.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ContributionApplicationStatusDto getMyApplicationStatus() {
        String userId = getCurrentUserId();

        ContributionApplication application = applicationRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("기여자 신청을 찾을 수 없습니다."));

        ContributionApplicationStatusDto dto = new ContributionApplicationStatusDto();
        dto.setUserId(application.getUserId());
        dto.setClientId(application.getClientId());
        dto.setStatus(application.getStatus().name());
        dto.setJobs(application.getJobs());
        dto.setMotivation(application.getMotivation());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());

        return dto;
    }

    public ClientSecretResponseDto generateClientSecret() {
        String userId = getCurrentUserId();

        ContributionApplication application = applicationRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("기여자 권한이 없습니다."));

        if (!application.getStatus().equals(ContributionApplication.Status.ACCEPT) &&
                !application.getStatus().equals(ContributionApplication.Status.TEMPORARY_ACCEPT)) {
            throw new ForbiddenException("승인된 기여자만 Client Secret을 발급받을 수 있습니다.");
        }

        String clientId = application.getClientId();

        // 기존 Secret 확인
        Optional<ClientSecret> existingSecret = clientSecretRepository.findById(clientId);
        if (existingSecret.isPresent() && existingSecret.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Secret이 만료되지 않았습니다.");
        }

        // 새 Secret 생성
        String newSecret = generateRandomSecret();

        // 유효기간 설정 (최초는 3일, 이후는 7일)
        int validDays = existingSecret.isEmpty() ? 3 : 7;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(validDays);

        ClientSecret clientSecret = new ClientSecret(clientId, newSecret, expiresAt);
        clientSecretRepository.save(clientSecret);

        return new ClientSecretResponseDto(
                clientId,
                newSecret,
                clientSecret.getCreatedAt(),
                expiresAt
        );
    }

    @Transactional(readOnly = true)
    public ContributorInfoDto getMyContributorInfo() {
        String userId = getCurrentUserId();

        ContributionApplication application = applicationRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("기여자 정보를 찾을 수 없습니다."));

        ContributorInfoDto dto = new ContributorInfoDto();
        dto.setUserId(application.getUserId());
        dto.setClientId(application.getClientId());
        dto.setStatus(application.getStatus().name());

        // Client Secret 정보 조회
        Optional<ClientSecret> secret = clientSecretRepository.findById(application.getClientId());
        if (secret.isPresent()) {
            dto.setClientSecret(new ClientSecretInfoDto(
                    secret.get().getCreatedAt(),
                    secret.get().getExpiresAt()
            ));
        } else {
            throw new BadRequestException("Secret이 만료되지 않았습니다.");
        }

        return dto;
    }

    public ContributorSessionDto getSessions() {
        String userId = getCurrentUserId();

        ContributionApplication application = applicationRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("기여자 권한이 없습니다."));

        // MongoDB 세션 조회 로직 (실제로는 MongoDB Repository 구현 필요)
        // 여기서는 더미 데이터 반환
        ContributorSessionDto dto = new ContributorSessionDto();
        dto.setSessionId("dummy-session-id");
        dto.setClientId(application.getClientId());
        dto.setIp("127.0.0.1");
        dto.setCreatedAt(LocalDateTime.now().minusHours(1));
        dto.setExpiresAt(LocalDateTime.now().plusHours(23));

        return dto;
    }

    public void deleteSessions() {
        String userId = getCurrentUserId();

        ContributionApplication application = applicationRepository.findById(userId)
                .orElseThrow(() -> new ForbiddenException("기여자 권한이 없습니다."));

        // MongoDB 세션 삭제 로직 구현 필요
        // 실제로는 MongoDB에서 clientId로 세션 찾아서 삭제
    }

    private String generateRandomSecret() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder secret = new StringBuilder();

        for (int i = 0; i < 32; i++) {
            secret.append(chars.charAt(random.nextInt(chars.length())));
        }

        return secret.toString();
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
