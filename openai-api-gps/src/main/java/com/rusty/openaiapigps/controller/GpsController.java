package com.rusty.openaiapigps.controller;

import com.rusty.openaiapigps.domain.dto.GpsDataDto;
import com.rusty.openaiapigps.domain.service.GpsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
