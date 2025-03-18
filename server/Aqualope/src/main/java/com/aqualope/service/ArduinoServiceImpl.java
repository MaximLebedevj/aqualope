package com.aqualope.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ArduinoServiceImpl implements ArduinoService {

    @Autowired
    private MqttServiceImpl mqttService;

    @Override
    public void sendLedCommand(boolean turnOn) throws Exception {
        log.info("Requesting to turn {} LED", turnOn ? "ON" : "OFF");
        mqttService.sendLedCommand(turnOn);
    }

    @Override
    public boolean isConnected() {
        return mqttService.isConnected();
    }
}