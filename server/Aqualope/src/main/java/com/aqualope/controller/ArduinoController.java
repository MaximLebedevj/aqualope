package com.aqualope.controller;

import com.aqualope.service.ArduinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/arduino")
@Slf4j
public class ArduinoController {

    @Autowired
    private ArduinoService arduinoService;

    @GetMapping("/led-on")
    public ResponseEntity<String> turnOnLed() {
        log.info("Received request to turn ON LED");
        try {
            arduinoService.sendLedCommand(true);
            return ResponseEntity.ok("LED ON command sent successfully");
        } catch (Exception e) {
            log.error("Failed to send LED ON command: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to send command: " + e.getMessage());
        }
    }

    @GetMapping("/led-off")
    public ResponseEntity<String> turnOffLed() {
        log.info("Received request to turn OFF LED");
        try {
            arduinoService.sendLedCommand(false);
            return ResponseEntity.ok("LED OFF command sent successfully");
        } catch (Exception e) {
            log.error("Failed to send LED OFF command: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to send command: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        log.info("Checking Arduino connection status");
        boolean isConnected = arduinoService.isConnected();
        return ResponseEntity.ok("Arduino connection status: " + (isConnected ? "Connected" : "Disconnected"));
    }
}
