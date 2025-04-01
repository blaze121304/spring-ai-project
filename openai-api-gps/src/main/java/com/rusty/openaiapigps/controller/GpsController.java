package com.rusty.openaiapigps.controller;

import com.rusty.openaiapigps.config.DynamicDataSource;
import com.rusty.openaiapigps.domain.dto.GpsDataDto;
import com.rusty.openaiapigps.domain.service.GpsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gps")
public class GpsController {

    private final GpsService gpsService;

    public GpsController(GpsService gpsService) {
        this.gpsService = gpsService;
    }

    @PostMapping
    public ResponseEntity<String> saveGpsData(@RequestBody GpsDataDto gpsDataDto) {
        gpsService.saveGpsData(gpsDataDto);
        return ResponseEntity.ok("GPS data saved successfully");
    }

    @GetMapping("/db1")
    public String useMainDb() {
        DynamicDataSource.setDataSourceKey("main");
        try {
            // mainDb 사용하는 로직
            return "DB IS CHANGE TO MAIN DB";
        } finally {
            DynamicDataSource.clearDataSourceKey();
        }
    }

    @GetMapping("/db2")
    public String useSecondaryDb() {
        DynamicDataSource.setDataSourceKey("secondary");
        try {
            // secondaryDb 사용하는 로직
            return "DB IS CHANGE TO SECOND DB";
        } finally {
            DynamicDataSource.clearDataSourceKey();
        }
    }


}
