package com.aqualope.controller;

import com.aqualope.model.WaterQuality;
import com.aqualope.service.MqttServiceImpl;
import com.aqualope.service.WaterQualityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class MonitoringController {

    @Autowired
    private MqttServiceImpl mqttService;

    @Autowired
    private WaterQualityServiceImpl waterQualityService;

    @PostMapping("/start-monitoring")
    public ResponseEntity<String> startMonitoring() {
        log.info("Received request to start monitoring");
        try {
            mqttService.startMonitoring();
            log.info("Monitoring started successfully");
            return ResponseEntity.ok("Water quality monitoring started");
        } catch (Exception e) {
            log.error("Failed to start monitoring: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to start monitoring: " + e.getMessage());
        }
    }

    @PostMapping("/stop-monitoring")
    public ResponseEntity<String> stopMonitoring() {
        log.info("Received request to stop monitoring");
        try {
            mqttService.stopMonitoring();
            log.info("Monitoring stopped successfully");
            return ResponseEntity.ok("Water quality monitoring stopped");
        } catch (Exception e) {
            log.error("Failed to stop monitoring: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to stop monitoring: " + e.getMessage());
        }
    }

    @GetMapping("/24hour")
    public ResponseEntity<List<WaterQuality>> getLast24HoursData() {
        log.info("Received request to get last 24 hours data");
        try {
            List<WaterQuality> data = waterQualityService.getLast24Hours();
            log.info("Retrieved {} records", data.size());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            log.error("Failed to get last 24 hours data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        try {
            long count = waterQualityService.getLast24Hours().size();
            return ResponseEntity.ok("System is running. Database has " + count + " records from the last 24 hours.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("System error: " + e.getMessage());
        }
    }
}