package com.aqualope.service;

import org.springframework.stereotype.Service;

public interface ArduinoService {
    void sendLedCommand(boolean turnOn) throws Exception;
    boolean isConnected();
}
