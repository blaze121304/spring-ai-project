//package com.rusty.openaiapigps.config;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//
//@Component
//public class DynamicDataSourceManager {
//
//    private HikariDataSource dataSource;
//    private final Environment environment;
//
//    public DynamicDataSourceManager(Environment environment) {
//        this.environment = environment;
//        initializeDataSource();
//    }
//
//    // 초기 데이터소스 생성
//    private void initializeDataSource() {
//        try {
//            dataSource = createDataSource("spring.datasource");
//        } catch (Exception e) {
//            // 예외 처리 추가
//            System.err.println("데이터 소스 초기화 중 오류 발생: " + e.getMessage());
//            throw e; // 또는 다른 적절한 예외 처리
//        }
//    }
//
//
//    private HikariDataSource createDataSource(String prefix) {
//        HikariConfig config = new HikariConfig();
//
//        String jdbcUrl = environment.getProperty(prefix + ".url");
//        String username = environment.getProperty(prefix + ".username");
//        String password = environment.getProperty(prefix + ".password");
//        String driverClassName = environment.getProperty(prefix + ".driver-class-name");
//
//        // 각각의 설정 값이 null인지 확인하고, null이면 예외 처리 또는 기본값 설정
//        if (jdbcUrl == null || username == null || password == null || driverClassName == null) {
//            throw new IllegalArgumentException("데이터베이스 연결 설정이 누락되었습니다. application.properties 파일을 확인하세요.");
//        }
//
//
//        config.setJdbcUrl(jdbcUrl);
//        config.setUsername(username);
//        config.setPassword(password);
//        config.setDriverClassName(driverClassName);
//
//        config.setMaximumPoolSize(10);
//
//        return new HikariDataSource(config);
//    }
//
//    // 기존 연결을 닫고, 새로운 연결로 변경
//    public synchronized void switchDataSource(String newDatasourcePrefix) {
//        if (dataSource != null && !dataSource.isClosed()) {
//            dataSource.close();
//        }
//        dataSource = createDataSource(newDatasourcePrefix);
//    }
//
//    public DataSource getCurrentDataSource() {
//        return dataSource;
//    }
//}
//
//
