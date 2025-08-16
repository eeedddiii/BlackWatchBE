package me.xyzo.blackwatchBE.repository;

import me.xyzo.blackwatchBE.document.LeakedDataDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeakedDataRepository extends MongoRepository<LeakedDataDocument, String> {

    // 기본 검색 - Spring Data MongoDB가 자동으로 쿼리 생성
    Page<LeakedDataDocument> findByHostContaining(String host, Pageable pageable);

    Page<LeakedDataDocument> findByTitleContaining(String title, Pageable pageable);

    Page<LeakedDataDocument> findByAuthorContaining(String author, Pageable pageable);

    Page<LeakedDataDocument> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    // 복합 검색을 위한 커스텀 쿼리
    @Query("{ $and: [ " +
            "?#{[0] == null ? {} : {'createdAt': {$gte: ?0}}}, " +
            "?#{[1] == null ? {} : {'createdAt': {$lte: ?1}}}, " +
            "?#{[2] == null ? {} : {'host': {$regex: ?2, $options: 'i'}}}, " +
            "?#{[3] == null ? {} : {'path': {$regex: ?3, $options: 'i'}}}, " +
            "?#{[4] == null ? {} : {'title': {$regex: ?4, $options: 'i'}}}, " +
            "?#{[5] == null ? {} : {'author': {$regex: ?5, $options: 'i'}}}, " +
            "?#{[6] == null ? {} : {'recordsCount': {$gte: ?6}}}, " +
            "?#{[7] == null ? {} : {'recordsCount': {$lte: ?7}}}, " +
            "?#{[8] == null ? {} : {'iocs': {$regex: ?8, $options: 'i'}}} " +
            "] }")
    Page<LeakedDataDocument> findWithFilters(
            LocalDateTime from, LocalDateTime to, String host,
            String pathContains, String titleContains, String author,
            Integer recordMin, Integer recordMax, String iocContains,
            Pageable pageable);

    // 전체 텍스트 검색
    @Query("{ $or: [ " +
            "{'title': {$regex: ?0, $options: 'i'}}, " +
            "{'article': {$regex: ?0, $options: 'i'}}, " +
            "{'iocs': {$regex: ?0, $options: 'i'}} " +
            "] }")
    Page<LeakedDataDocument> findByTextSearch(String searchText, Pageable pageable);

    // 개인정보 검색
    @Query("{ $or: [ " +
            "{'leakedEmail': {$in: ?0}}, " +
            "{'leakedName': {$in: ?1}} " +
            "] }")
    List<LeakedDataDocument> findByPersonalData(List<String> emails, List<String> names);

    // 이메일로 검색
    @Query("{'leakedEmail': {$in: ?0}}")
    List<LeakedDataDocument> findByLeakedEmailIn(List<String> emails);

    // 이름으로 검색
    @Query("{'leakedName': {$in: ?0}}")
    List<LeakedDataDocument> findByLeakedNameIn(List<String> names);
}