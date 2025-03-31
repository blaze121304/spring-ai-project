package com.rusty.openaiapigps.repository;

import com.rusty.openaiapigps.domain.entity.GpsData;
import org.springframework.data.jpa.repository.JpaRepository;

public class GpsRepository extends JpaRepository<GpsData, Long> {
}
