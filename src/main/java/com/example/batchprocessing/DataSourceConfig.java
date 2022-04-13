package com.example.batchprocessing;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Qualifier("controlDB")
    @Primary
    public DataSource getBatchDBDataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:controldb")
                .username("sa")
                .password("")
                .build();
    }

    @Bean
    @Qualifier("sourceDB")
    @ConfigurationProperties(prefix="spring.source-db")
    public DataSource getSourceDBDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean
    @Qualifier("sourceDB")
    public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDB") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("targetDB")
    @ConfigurationProperties(prefix="spring.target-db")
    public DataSource getTargetDBDataSource() {
        return DataSourceBuilder
                .create()
                .build();
    }

    @Bean
    @Qualifier("targetDB")
    public JdbcTemplate targetJdbcTemplate(@Qualifier("targetDB") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
