package com.rusty.openaiapigps.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

//    @Primary
//    @Bean(name="mainDataSource")
//    @ConfigurationProperties(prefix = "sou-a")
//    public DataSource dataSourceA(){
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name="backupDataSource")
//    @ConfigurationProperties(prefix = "sou-b")
//    public DataSource dataSourceB(){
//        return DataSourceBuilder.create().build();
//    }

//    @Bean(name="mainJdbcTemplate")
//    public JdbcTemplate mainJdbcTemplate(@Qualifier("mainDataSource") DataSource primaryDataSource){
//        return new JdbcTemplate(primaryDataSource);
//    }
//
//    @Bean(name="backupJdbcTemplate")
//    public JdbcTemplate backupJdbcTemplate(@Qualifier("backupDataSource") DataSource secondaryDataSource){
//        return new JdbcTemplate(secondaryDataSource);
//    }
//
//    @Bean
//    public List<JdbcTemplate> jdbcTemplateList() {
//        return new ArrayList<>();
//    }

    @Bean
    @Primary // 여러 DataSource Bean이 있을 경우, 이 Bean을 Primary로 설정
    public DynamicDataSource dynamicDataSource(DataSource dataSourceA, DataSource dataSourceB) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("dataSourceA", dataSourceA);
        targetDataSources.put("dataSourceB", dataSourceB);
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(dataSourceA); // 기본 데이터 소스 설정
        return dynamicDataSource;
    }



}
