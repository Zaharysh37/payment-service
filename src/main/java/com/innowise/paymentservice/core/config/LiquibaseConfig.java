package com.innowise.paymentservice.core.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseConfig {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Value("${spring.liquibase.change-log}")
    private String changeLog;

    @Bean
    public String runLiquibase() {
        try {

            Database database = DatabaseFactory.getInstance()
                .openDatabase(uri, null, null, null, new ClassLoaderResourceAccessor());

            try(Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("");
                return uri;
            }
        } catch (Exception e) {
            throw new RuntimeException("Liquibase update failed", e);
        }
    }
}
