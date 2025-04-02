package com.rusty.openaiapigps.config;

import com.rusty.openaiapigps.config.datasource.LookupKey;
import com.rusty.openaiapigps.config.datasource.RoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
@Slf4j
@RequiredArgsConstructor
public class DataSourceManager {

    private AbstractRoutingDataSource routingDataSource;
    private final Map<Object, Object> dataSourceMap = new ConcurrentHashMap<>();
    private final Environment env;

    public DataSource createMultiDataSource() {
        HikariDataSource defaultDataSource = loadDefaultDataSource();
        routingDataSource = new RoutingDataSource();
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.afterPropertiesSet();

        setDataSourcePool();

        return routingDataSource;
    }

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

    private HikariDataSource createDataSource(LookupKey lookupKey) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(lookupKey.getUrl());
        dataSource.setUsername(lookupKey.getUserName());
        dataSource.setPassword(lookupKey.getPassword());
        dataSource.setDriverClassName(lookupKey.getDriver());
        dataSource.setMaximumPoolSize(10);

        return dataSource;
    }

    public void setCurrent(LookupKey lookupKey) {
        if (isAbsent(lookupKey)) {
            addDataSource(lookupKey);
        }
    }

    public boolean isAbsent(LookupKey key) {
        return !dataSourceMap.containsKey(key);
    }

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
