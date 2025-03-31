package com.rusty.openaiapigps.domain.service;

import com.rusty.openaiapigps.domain.dto.GpsDataDto;
import com.rusty.openaiapigps.domain.entity.GpsData;
import com.rusty.openaiapigps.repository.GpsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GpsService {

    private final GpsRepository gpsRepository;

    @Autowired
    public GpsService(GpsRepository gpsRepository) {
        this.gpsRepository = gpsRepository;
    }

    public void saveGpsData(GpsDataDto dto) {
        GpsData gpsData = new GpsData();
        gpsData.setLatitude(dto.getLatitude());
        gpsData.setLongitude(dto.getLongitude());
        gpsRepository.save(gpsData);
    }
}
