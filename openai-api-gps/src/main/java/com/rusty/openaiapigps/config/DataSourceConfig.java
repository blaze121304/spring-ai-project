package com.rusty.openaiapigps.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

    private AbstractRoutingDataSource routingDataSource;
    private final Map<Object, Object> dataSourceMap = new ConcurrentHashMap<>();
    private final Environment env;

    /**
     * 데이터 소스 초기화
     *
     * @return 초기화된 라우팅 데이터 소스
     */
    public DataSource createMultiDataSource() {
        HikariDataSource defaultDataSource = loadDefaultDataSource();
        routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.afterPropertiesSet();

        setDataSourcePool();

        return routingDataSource;
    }

    /**
     * 환경 설정을 기반으로 기본 데이터 소스 로드
     *
     * @return 로드된 기본 데이터 소스
     */
    public HikariDataSource loadDefaultDataSource() {
        HikariDataSource defaultDataSource = new HikariDataSource();

        defaultDataSource.setPoolName("Default");
        defaultDataSource.setJdbcUrl(env.getProperty("spring.datasource.url"));
        defaultDataSource.setUsername(env.getProperty("spring.datasource.username"));
        defaultDataSource.setPassword(env.getProperty("spring.datasource.password"));
        defaultDataSource.setDriverClassName(env.getProperty("spring.datasource.hikari.driver-class-name"));
        defaultDataSource.setMaximumPoolSize(Integer.parseInt(Objects.requireNonNull(env.getProperty("spring.datasource.hikari.maximumPoolSize"))));

        log.debug("Set Default DataSource: " + defaultDataSource.getJdbcUrl());

        return defaultDataSource;
    }


    /**
     * 데이터베이스에서 데이터 소스 설정 조회
     * <p>
     * 조회된 각 데이터 소스를 dataSourceMap에 추가합니다.
     */
    private void setDataSourcePool() {
        DataSource defaultDataSource = routingDataSource.getResolvedDefaultDataSource();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(defaultDataSource));
        String sql = "SELECT * FROM datasource_map";
        jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    LookupKey lookupKey = LookupKey.builder()
                            .url(rs.getString("url"))
                            .userName(rs.getString("user_name"))
                            .password(rs.getString("password"))
                            .driver(rs.getString("driver"))
                            .build();

                    HikariDataSource dataSource = createDataSource(lookupKey);

                    dataSourceMap.put(lookupKey, dataSource);

                    return dataSource;
                }
        );
    }

    /**
     * 주어진 설정을 바탕으로 새로운 HikariDataSource 생성
     *
     * @param lookupKey 데이터 소스 설정 정보
     * @return 생성된 HikariDataSource
     */
    private HikariDataSource createDataSource(LookupKey lookupKey) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(lookupKey.getUrl());
        dataSource.setUsername(lookupKey.getUserName());
        dataSource.setPassword(lookupKey.getPassword());
        dataSource.setDriverClassName(lookupKey.getDriver());
        dataSource.setMaximumPoolSize(10);

        return dataSource;
    }

    /**
     * 주어진 LookupKey에 해당하는 데이터 소스를 현재 컨텍스트에 설정
     * <p>
     * 존재하지 않는 경우, 데이터 소스를 추가합니다.
     *
     * @param lookupKey 설정할 데이터 소스의 키
     */
    public void setCurrent(LookupKey lookupKey) {
        if (isAbsent(lookupKey)) {
            addDataSource(lookupKey);
        }
    }

    /**
     * 주어진 키에 해당하는 데이터 소스가 dataSourceMap에 존재하는지 체크
     *
     * @param key 확인할 데이터 소스의 키
     * @return 존재 여부
     */
    public boolean isAbsent(LookupKey key) {
        return !dataSourceMap.containsKey(key);
    }

    /**
     * 새로운 데이터 소스를 dataSourceMap에 추가
     *
     * @param lookupKey 추가할 데이터 소스의 키
     */
    public void addDataSource(LookupKey lookupKey) {
        if (!dataSourceMap.containsKey(lookupKey)) {
            HikariDataSource dataSource = createDataSource(lookupKey);

            try (Connection c = dataSource.getConnection()) {
                dataSourceMap.put(lookupKey, dataSource);
                routingDataSource.afterPropertiesSet();
                log.debug("Added DataSource: " + lookupKey.getUrl());
            } catch (SQLException e) {
                log.error("Error adding DataSource: " + e.getMessage(), e);
                throw new IllegalArgumentException("Invalid connection information.", e);
            }
        }
    }


}
