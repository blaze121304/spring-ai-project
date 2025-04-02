package com.rusty.openaiapigps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * RoutingDataSource Configuration
 */
@Configuration
@EnableTransactionManagement
public class RoutingDatasourceConfig {

    /**
     * 다중 데이터 소스 구성을 위한 AbstractRoutingDataSource 생성
     * <p>
     * DataSourceManager를 사용하여 다중 데이터 소스를 생성합니다.
     *
     * @param dataSourceManager 데이터 소스를 관리하는 매니저 객체
     * @return 구성된 데이터 소스
     */
    @Bean
    public DataSource routingDataSource(DataSourceManager dataSourceManager) {
        return dataSourceManager.createMultiDataSource();
    }

    /**
     * 지연 연결(Lazy Connection) 데이터 소스 구성
     * <p>
     * @Transactional 어노테이션이 적용된 메소드에 진입할 때마다 실제 데이터베이스 연결을 지연시켜 필요한 순간에만 연결을 수립합니다.
     *
     * @param dataSource 기본 데이터 소스
     * @return 지연 연결을 적용한 데이터 소스
     */
    @Bean
    @Primary
    public DataSource lazyDataSource(@Qualifier("routingDataSource") DataSource dataSource) {
        return new LazyConnectionDataSourceProxy(dataSource);
    }

    /**
     * 지연 연결 데이터 소스를 사용하여 TransactionManger 구성
     *
     * @param lazyRoutingDataSource 지연 연결이 적용된 데이터 소스
     * @return 구성된 플랫폼 트랜잭션 관리자
     */
    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier(value = "lazyDataSource") DataSource lazyRoutingDataSource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(lazyRoutingDataSource);
        return transactionManager;
    }

}