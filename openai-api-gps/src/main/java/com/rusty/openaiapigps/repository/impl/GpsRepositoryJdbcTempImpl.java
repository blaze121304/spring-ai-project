package com.rusty.openaiapigps.repository.impl;

import com.rusty.openaiapigps.config.DynamicDataSource;
import com.rusty.openaiapigps.domain.dto.GpsDataDto;
//import com.rusty.openaiapigps.domain.entity.GpsData;
import com.rusty.openaiapigps.repository.GpsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GpsRepositoryJdbcTempImpl implements GpsRepository {

    private final List<JdbcTemplate> jdbcTemplateList;

    public GpsRepositoryJdbcTempImpl(List<JdbcTemplate> jdbcTemplateList) {
        this.jdbcTemplateList = jdbcTemplateList;
    }

    @Override
    public void save(GpsDataDto gpsDataDto) {
        jdbcTemplateList.getFirst().update("INSERT INTO gps_data (latitude, longitude) VALUES (?, ?)",
                gpsDataDto.getLatitude(), gpsDataDto.getLongitude());
    }




}
