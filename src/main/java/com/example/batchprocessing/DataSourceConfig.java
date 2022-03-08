package com.example.batchprocessing;

import org.springframework.beans.factory.annotation.Qualifier;
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
                .url("jdbc:h2:mem:testdb")
                .username("sa")
                .password("")
                .build();
    }

    @Bean
    @Qualifier("sourceDB")
    public DataSource getSourceDBDataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.mariadb.jdbc.Driver")
                .url("jdbc:mysql://localhost/source_db")
                .username("source_user")
                .password("")
                .build();
    }

    @Bean
    @Qualifier("sourceDB")
    public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDB") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("targetDB")
    public DataSource getTargetDBDataSource() {
        return DataSourceBuilder
                .create()
                .driverClassName("org.mariadb.jdbc.Driver")
                .url("jdbc:mysql://localhost/target_db")
                .username("target_user")
                .password("")
                .build();
    }

    @Bean
    @Qualifier("targetDB")
    public JdbcTemplate targetJdbcTemplate(@Qualifier("targetDB") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
