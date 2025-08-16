package me.xyzo.blackwatchBE.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexConfig implements ApplicationRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createLeakedDataIndexes();
        createVulnerabilityDataIndexes();
    }

    private void createLeakedDataIndexes() {
        // 기본 검색용 인덱스
        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("host", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("title", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("author", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("createdAt", org.springframework.data.domain.Sort.Direction.DESC));

        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("recordsCount", org.springframework.data.domain.Sort.Direction.ASC));

        // 개인정보 검색용 인덱스
        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("leakedEmail", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index().on("leakedName", org.springframework.data.domain.Sort.Direction.ASC));

        // 텍스트 검색용 인덱스
        mongoTemplate.indexOps("leaked_data")
                .ensureIndex(new Index()
                        .on("title", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("article", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("iocs", org.springframework.data.domain.Sort.Direction.ASC));
    }

    private void createVulnerabilityDataIndexes() {
        // 기본 검색용 인덱스
        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("host", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("title", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("author", org.springframework.data.domain.Sort.Direction.ASC));

        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("createdAt", org.springframework.data.domain.Sort.Direction.DESC));

        // CVE 검색용 인덱스
        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("cveIds", org.springframework.data.domain.Sort.Direction.ASC));

        // CVSS 범위 검색용 인덱스
        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("cvss", org.springframework.data.domain.Sort.Direction.ASC));

        // 취약점 분류 검색용 인덱스
        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index().on("vulnerabilityClass", org.springframework.data.domain.Sort.Direction.ASC));

        // 텍스트 검색용 인덱스
        mongoTemplate.indexOps("vulnerability_data")
                .ensureIndex(new Index()
                        .on("title", org.springframework.data.domain.Sort.Direction.ASC)
                        .on("article", org.springframework.data.domain.Sort.Direction.ASC));
    }
}
