package com.aqualope.controller;

import com.aqualope.service.MqttServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class AquariumMonitoringController {

    @Autowired
    private MqttServiceImpl mqttService;

    @PostMapping("/aqua-start-monitoring")
    public ResponseEntity<String> startAquaMonitoring(@RequestBody Map<String, Long> request) {
        log.info("Received request to start aquarium monitoring");

        Long aquariumId = request.get("aquariumId");
        if (aquariumId == null) {
            return ResponseEntity.badRequest().body("Aquarium ID must be provided");
        }

        try {
            mqttService.startAquaMonitoring(aquariumId);
            log.info("Aquarium monitoring started successfully for aquarium id: {}", aquariumId);
            return ResponseEntity.ok("Aquarium monitoring started for aquarium id: " + aquariumId);
        } catch (Exception e) {
            log.error("Failed to start aquarium monitoring: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to start aquarium monitoring: " + e.getMessage());
        }
    }

    @PostMapping("/aqua-stop-monitoring")
    public ResponseEntity<String> stopAquaMonitoring() {
        log.info("Received request to stop aquarium monitoring");
        try {
            mqttService.stopAquaMonitoring();
            log.info("Aquarium monitoring stopped successfully");
            return ResponseEntity.ok("Aquarium monitoring stopped");
        } catch (Exception e) {
            log.error("Failed to stop aquarium monitoring: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to stop aquarium monitoring: " + e.getMessage());
        }
    }
}