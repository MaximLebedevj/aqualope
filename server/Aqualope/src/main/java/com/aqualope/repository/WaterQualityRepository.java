package com.aqualope.repository;

import com.aqualope.model.WaterQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface WaterQualityRepository extends JpaRepository<WaterQuality, Long> {

    @Query("SELECT w FROM WaterQuality w WHERE w.timestamp >= :startTime ORDER BY w.timestamp DESC")
    List<WaterQuality> getWaterQualityLastDay(@Param("startTime") LocalDateTime startTime);
}
