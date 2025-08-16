package me.xyzo.blackwatchBE.service;

import me.xyzo.blackwatchBE.document.LeakedDataDocument;
import me.xyzo.blackwatchBE.document.VulnerabilityDataDocument;
import me.xyzo.blackwatchBE.dto.*;
import me.xyzo.blackwatchBE.dto.mongo.*;
import me.xyzo.blackwatchBE.exception.NotFoundException;
import me.xyzo.blackwatchBE.repository.LeakedDataRepository;
import me.xyzo.blackwatchBE.repository.VulnerabilityDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MongoDataService {

    @Autowired
    private LeakedDataRepository leakedDataRepository;

    @Autowired
    private VulnerabilityDataRepository vulnerabilityDataRepository;

    @Autowired
    private WebClient mongoApiClient;

    @Value("${mongo.api.session-key}")
    private String sessionKey;

    // ======= 유출 데이터 조회 메서드 =======

    public Page<Map<String, Object>> getLeakedData(
            LocalDateTime from, LocalDateTime to, List<String> hosts,
            String pathContains, String titleContains, String author,
            Integer recordMin, Integer recordMax, String iocContains,
            String q, String projection, Pageable pageable) {

        Page<LeakedDataDocument> documents;

        if (q != null && !q.trim().isEmpty()) {
            // 전체 텍스트 검색
            documents = leakedDataRepository.findByTextSearch(q, pageable);
        } else {
            // 필터 검색
            String host = hosts != null && !hosts.isEmpty() ? hosts.get(0) : null;
            documents = leakedDataRepository.findWithFilters(
                    from, to, host, pathContains, titleContains, author,
                    recordMin, recordMax, iocContains, pageable);
        }

        // 개인정보 제거하고 Map으로 변환
        return documents.map(this::sanitizeLeakedData);
    }

    public LeakedDataDocument getLeakedDataDetail(String id) {
        LeakedDataDocument document = leakedDataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("데이터를 찾을 수 없습니다."));

        // 개인정보 제거
        return sanitizeLeakedDataDocument(document);
    }

    public PersonalDataSearchResultDto findPersonalData(PersonalDataSearchDto request) {
        if ((request.getEmails() == null || request.getEmails().isEmpty()) &&
                (request.getNames() == null || request.getNames().isEmpty())) {
            return new PersonalDataSearchResultDto(new ArrayList<>(), 0);
        }

        List<PersonalDataMatchDto> matches = new ArrayList<>();
        int totalFound = 0;

        if (request.getEmails() != null && !request.getEmails().isEmpty()) {
            List<LeakedDataDocument> emailMatches =
                    leakedDataRepository.findByLeakedEmailIn(request.getEmails());

            for (String email : request.getEmails()) {
                List<String> leakIds = emailMatches.stream()
                        .filter(doc -> doc.getLeakedEmail() != null && doc.getLeakedEmail().contains(email))
                        .map(LeakedDataDocument::getId)
                        .collect(Collectors.toList());

                boolean found = !leakIds.isEmpty();
                if (found) totalFound++;
                matches.add(new PersonalDataMatchDto(email, null, found, leakIds));
            }
        }

        if (request.getNames() != null && !request.getNames().isEmpty()) {
            List<LeakedDataDocument> nameMatches =
                    leakedDataRepository.findByLeakedNameIn(request.getNames());

            for (String name : request.getNames()) {
                List<String> leakIds = nameMatches.stream()
                        .filter(doc -> doc.getLeakedName() != null && doc.getLeakedName().contains(name))
                        .map(LeakedDataDocument::getId)
                        .collect(Collectors.toList());

                boolean found = !leakIds.isEmpty();
                if (found) totalFound++;
                matches.add(new PersonalDataMatchDto(null, name, found, leakIds));
            }
        }

        return new PersonalDataSearchResultDto(matches, totalFound);
    }

    // ======= 취약점 데이터 조회 메서드 =======

    public Page<Map<String, Object>> getVulnerabilityData(
            LocalDateTime from, LocalDateTime to, List<String> hosts,
            String pathContains, String titleContains, String author,
            List<String> cves, Double cvssMin, Double cvssMax,
            String vulnClass, String q, String projection, Pageable pageable) {

        Page<VulnerabilityDataDocument> documents;

        if (q != null && !q.trim().isEmpty()) {
            // 전체 텍스트 검색
            documents = vulnerabilityDataRepository.findByTextSearch(q, pageable);
        } else {
            // 필터 검색
            String host = hosts != null && !hosts.isEmpty() ? hosts.get(0) : null;
            documents = vulnerabilityDataRepository.findWithFilters(
                    from, to, host, pathContains, titleContains, author,
                    cves, cvssMin, cvssMax, vulnClass, pageable);
        }

        return documents.map(this::convertVulnerabilityToMap);
    }

    public VulnerabilityDataDocument getVulnerabilityDataDetail(String id) {
        return vulnerabilityDataRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("데이터를 찾을 수 없습니다."));
    }

    // ======= 데이터 입력 메서드 (FastAPI 호출) =======

    public MongoApiResponse<String> submitLeakedData(String userId, MongoLeakedDataDto data) {
        try {
            return mongoApiClient.post()
                    .uri("/data/leaked")
                    .header("X-Session-Key", sessionKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(MongoApiResponse.class)
                    .block();
        } catch (Exception e) {
            MongoApiResponse<String> errorResponse = new MongoApiResponse<>();
            errorResponse.setResult("ERROR");
            errorResponse.setReason("API 호출 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    public MongoApiResponse<String> submitVulnerabilityData(String userId, MongoVulnerabilityDataDto data) {
        try {
            return mongoApiClient.post()
                    .uri("/data/vulnerability")
                    .header("X-Session-Key", sessionKey)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(MongoApiResponse.class)
                    .block();
        } catch (Exception e) {
            MongoApiResponse<String> errorResponse = new MongoApiResponse<>();
            errorResponse.setResult("ERROR");
            errorResponse.setReason("API 호출 실패: " + e.getMessage());
            return errorResponse;
        }
    }

    // ======= 헬퍼 메서드 =======

    private Map<String, Object> sanitizeLeakedData(LeakedDataDocument data) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", data.getId());
        result.put("clientId", data.getClientId());
        result.put("host", data.getHost());
        result.put("path", data.getPath());
        result.put("title", data.getTitle());
        result.put("author", data.getAuthor());
        result.put("uploadDate", data.getUploadDate());
        result.put("leakType", data.getLeakType());
        result.put("recordsCount", data.getRecordsCount());
        result.put("iocs", data.getIocs());
        result.put("price", data.getPrice());

        // 개인정보 제거
        String sanitizedArticle = data.getArticle();
        if (sanitizedArticle != null) {
            sanitizedArticle = sanitizedArticle.replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[이메일]");
            sanitizedArticle = sanitizedArticle.replaceAll("[가-힣]{3,}", "[이름]");
        }
        result.put("article", sanitizedArticle);
        result.put("ref", data.getRef());
        result.put("createdAt", data.getCreatedAt());

        // leakedEmail, leakedName은 제외

        return result;
    }

    private LeakedDataDocument sanitizeLeakedDataDocument(LeakedDataDocument data) {
        // 개인정보 필드 제거
        data.setLeakedEmail(null);
        data.setLeakedName(null);

        // article에서 개인정보 마스킹
        if (data.getArticle() != null) {
            String sanitized = data.getArticle()
                    .replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[이메일]")
                    .replaceAll("[가-힣]{3,}", "[이름]");
            data.setArticle(sanitized);
        }

        return data;
    }

    private Map<String, Object> convertVulnerabilityToMap(VulnerabilityDataDocument data) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", data.getId());
        result.put("clientId", data.getClientId());
        result.put("host", data.getHost());
        result.put("path", data.getPath());
        result.put("title", data.getTitle());
        result.put("author", data.getAuthor());
        result.put("uploadDate", data.getUploadDate());
        result.put("cveIds", data.getCveIds());
        result.put("cvss", data.getCvss());
        result.put("vulnerabilityClass", data.getVulnerabilityClass());
        result.put("products", data.getProducts());
        result.put("exploitationTechnique", data.getExploitationTechnique());
        result.put("article", data.getArticle());
        result.put("ref", data.getRef());
        result.put("createdAt", data.getCreatedAt());

        return result;
    }
}