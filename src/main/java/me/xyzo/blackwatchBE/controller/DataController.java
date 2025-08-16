package me.xyzo.blackwatchBE.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.xyzo.blackwatchBE.document.LeakedDataDocument;
import me.xyzo.blackwatchBE.document.VulnerabilityDataDocument;
import me.xyzo.blackwatchBE.dto.PersonalDataSearchDto;
import me.xyzo.blackwatchBE.dto.PersonalDataSearchResultDto;
import me.xyzo.blackwatchBE.dto.mongo.*;
import me.xyzo.blackwatchBE.service.DataService;
import me.xyzo.blackwatchBE.service.MongoDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "정보 조회", description = "MongoDB 직접 연결 기반 데이터 조회 및 입력 API")
public class DataController {

    @Autowired
    private DataService dataService;

    @Autowired
    private MongoDataService mongoDataService;

    @GetMapping("/leaked")
    @Operation(summary = "유출 데이터 일괄 조회", description = "MongoDB에서 직접 유출된 데이터를 검색")
    public ResponseEntity<Page<Map<String, Object>>> getLeakedData(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "-createdAt") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) String pathContains,
            @RequestParam(required = false) String titleContains,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer recordMin,
            @RequestParam(required = false) Integer recordMax,
            @RequestParam(required = false) String iocContains,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String projection) {

        Sort sortObj = sort.startsWith("-")
                ? Sort.by(Sort.Direction.DESC, sort.substring(1))
                : Sort.by(Sort.Direction.ASC, sort);

        if (limit > 1000) limit = 1000;
        Pageable pageable = PageRequest.of(page, limit, sortObj);

        List<String> hosts = host != null ? Arrays.asList(host.split(",")) : null;

        Page<Map<String, Object>> result = dataService.getLeakedData(
                from, to, hosts, pathContains, titleContains, author,
                recordMin, recordMax, iocContains, q, projection, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/leaked/{id}")
    @Operation(summary = "유출 데이터 세부 조회", description = "MongoDB에서 직접 데이터를 상세 조회")
    public ResponseEntity<LeakedDataDocument> getLeakedDataDetail(@PathVariable String id) {
        LeakedDataDocument result = dataService.getLeakedDataDetail(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/leaked/find")
    @Operation(summary = "개인정보 유출 여부 조회", description = "MongoDB에서 직접 개인정보 유출 데이터 존재 여부 검색")
    public ResponseEntity<PersonalDataSearchResultDto> findPersonalData(@RequestBody PersonalDataSearchDto request) {
        PersonalDataSearchResultDto result = dataService.findPersonalData(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vulnerability")
    @Operation(summary = "취약점 데이터 일괄 조회", description = "MongoDB에서 직접 취약점 데이터를 검색")
    public ResponseEntity<Page<Map<String, Object>>> getVulnerabilityData(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "-createdAt") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) String pathContains,
            @RequestParam(required = false) String titleContains,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String cve,
            @RequestParam(required = false) Double cvssMin,
            @RequestParam(required = false) Double cvssMax,
            @RequestParam(required = false) String vulnClass,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String projection) {

        Sort sortObj = sort.startsWith("-")
                ? Sort.by(Sort.Direction.DESC, sort.substring(1))
                : Sort.by(Sort.Direction.ASC, sort);

        if (limit > 1000) limit = 1000;
        Pageable pageable = PageRequest.of(page, limit, sortObj);

        List<String> hosts = host != null ? Arrays.asList(host.split(",")) : null;
        List<String> cves = cve != null ? Arrays.asList(cve.split(",")) : null;

        Page<Map<String, Object>> result = dataService.getVulnerabilityData(
                from, to, hosts, pathContains, titleContains, author,
                cves, cvssMin, cvssMax, vulnClass, q, projection, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/vulnerability/{id}")
    @Operation(summary = "취약점 데이터 세부 조회", description = "MongoDB에서 직접 데이터를 상세 조회")
    public ResponseEntity<VulnerabilityDataDocument> getVulnerabilityDataDetail(@PathVariable String id) {
        VulnerabilityDataDocument result = dataService.getVulnerabilityDataDetail(id);
        return ResponseEntity.ok(result);
    }

    // 데이터 입력은 FastAPI를 통해 (인증이 필요하므로)
    @PostMapping("/leaked")
    @Operation(summary = "유출 데이터 입력", description = "FastAPI를 통해 MongoDB로 유출 데이터 전송")
    public ResponseEntity<MongoApiResponse<String>> submitLeakedData(@RequestBody MongoLeakedDataDto data) {
        String userId = getCurrentUserId();
        MongoApiResponse<String> result = mongoDataService.submitLeakedData(userId, data);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/vulnerability")
    @Operation(summary = "취약점 데이터 입력", description = "FastAPI를 통해 MongoDB로 취약점 데이터 전송")
    public ResponseEntity<MongoApiResponse<String>> submitVulnerabilityData(@RequestBody MongoVulnerabilityDataDto data) {
        String userId = getCurrentUserId();
        MongoApiResponse<String> result = mongoDataService.submitVulnerabilityData(userId, data);
        return ResponseEntity.ok(result);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}