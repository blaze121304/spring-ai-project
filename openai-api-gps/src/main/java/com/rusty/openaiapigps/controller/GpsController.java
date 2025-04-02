package com.rusty.openaiapigps.controller;

import com.rusty.openaiapigps.config.DynamicDataSource;
import com.rusty.openaiapigps.config.DynamicDataSourceManager;
import com.rusty.openaiapigps.domain.dto.GpsDataDto;
import com.rusty.openaiapigps.domain.service.GpsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gps")
public class GpsController {

    private final GpsService gpsService;
    private final DynamicDataSourceManager dynamicDataSourceManager;

    public GpsController(GpsService gpsService, DynamicDataSourceManager dynamicDataSourceManager) {
        this.gpsService = gpsService;
        this.dynamicDataSourceManager = dynamicDataSourceManager;
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

    @GetMapping("/dbchange")
    public String useMainDb1() {
        if(dynamicDataSourceManager.getCurrentDataSource().equals("main")){
            dynamicDataSourceManager.switchDataSource( "backup");
        }
        return "DB IS CHANGE TO MAIN DB";
    }



}
