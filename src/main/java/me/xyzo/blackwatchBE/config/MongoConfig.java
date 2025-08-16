package me.xyzo.blackwatchBE.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "blackwatch_data";
    }

    // 필요시 추가 설정
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}