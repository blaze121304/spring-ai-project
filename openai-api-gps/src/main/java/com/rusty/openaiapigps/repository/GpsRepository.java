package com.rusty.openaiapigps.repository;

import com.rusty.openaiapigps.domain.dto.GpsDataDto;
//import com.rusty.openaiapigps.domain.entity.GpsData;

public interface GpsRepository {
    void save(GpsDataDto gpsDataDto);
//    void save(GpsData gpsData);
}
