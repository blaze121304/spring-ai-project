package com.rusty.openaiapigps.domain.service;

import com.rusty.openaiapigps.domain.dto.GpsDataDto;
//import com.rusty.openaiapigps.domain.entity.GpsData;
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

    public void saveGpsData(GpsDataDto gpsDataDto) {

//        GpsData gpsData = new GpsData();
//
//        gpsData.setLatitude(gpsDataDto.getLatitude());
//        gpsData.setLongitude(gpsDataDto.getLongitude());

        //gpsRepository.save(gpsData);        //jpa
        gpsRepository.save(gpsDataDto);     //jdbctemplate
    }
}
