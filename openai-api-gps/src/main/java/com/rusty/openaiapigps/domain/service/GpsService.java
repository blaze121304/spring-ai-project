package com.rusty.openaiapigps.domain.service;

import com.rusty.openaiapigps.domain.dto.GpsDataDto;
//import com.rusty.openaiapigps.domain.entity.GpsData;
import com.rusty.openaiapigps.repository.GpsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    @Transactional
    public String getDataSource() throws InterruptedException {
        log.info("DataSource 조회");

        // 무거운 비즈니스 로직
        Thread.sleep(100000L);

        // 실제로 커넥션이 필요한 시점
        dataSourceMapRepository.findById(1L);
        return "조회 완료";
    }
}
