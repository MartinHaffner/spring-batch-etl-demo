package com.example.batchprocessing;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Configuration
@EnableAutoConfiguration
@EnableBatchProcessing
public class BatchConfiguration extends DefaultBatchConfigurer {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
    Logger logger = Logger.getLogger("BatchConfiguration");
    Set<String> sourceIDs;

    @Bean
    public JdbcCursorItemReader<Person> dbReader(
            @Qualifier("sourceDB") DataSource dataSource,
            @Value("${spring.source-db.table}") String sourceTableName) {

        return new JdbcCursorItemReaderBuilder<Person>()
                .name("personReader")
                .dataSource(dataSource)
                .sql("SELECT first_name, last_name FROM " + sourceTableName)
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .build();
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(
            @Qualifier("targetDB") DataSource dataSource,
            @Value("${spring.target-db.table}") String targetTableName
    ) {
        return new JdbcBatchItemWriterBuilder<Person>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO " + targetTableName + " (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener,
                             @Qualifier("truncateStep")
                                     Step step0,
                             @Qualifier("loadStep")
                                     Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step0)
                .next(step1)
                .build();
    }

    @Bean
    @Qualifier("loadStep")
    public Step loadStep(JdbcCursorItemReader<Person> reader, JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("loadStep")
                .<Person, Person>chunk(10)
                .reader(reader)
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    @Qualifier("truncateStep")
    public Step truncateStep(@Qualifier("targetDB") DataSource dataSource) {
        return stepBuilderFactory
                .get("truncateStep")
                .tasklet((stepContribution, chunkContext) -> {
                    new JdbcTemplate(dataSource).execute("TRUNCATE TABLE people");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


}
