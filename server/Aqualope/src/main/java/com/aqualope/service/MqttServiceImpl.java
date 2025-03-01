package com.aqualope.service;

import com.aqualope.model.WaterQuality;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;

@Service
@Slf4j
public class MqttServiceImpl {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;

    private MqttClient mqttClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private WaterQualityServiceImpl waterQualityServiceImpl;

    @PostConstruct
    public void init() throws MqttException {
        mqttClient = new MqttClient(broker, clientId);
        connect();
    }

    private void connect() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);

        mqttClient.connect(options);
        log.info("Connected to broker: {}", broker);

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("Connection to broker lost: {}", cause.getMessage());
                try {
                    if (!mqttClient.isConnected()) {
                        mqttClient.connect(options);
                        if(isMonitoring.get()) {
                            mqttClient.subscribe(topic);
                        }
                    }
                } catch (MqttException e) {
                    log.error("Failed to reconnect to broker: {}", e.getMessage());
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (isMonitoring.get()) {
                    log.info("Received message: {}", message);
                    String payload = new String(message.getPayload());

                    try {
                        JsonNode jsonNode = objectMapper.readTree(payload);

                        WaterQuality data = new WaterQuality(
                                getDoubleValue(jsonNode, "temperature"),
                                getDoubleValue(jsonNode, "oxygen_saturation"),
                                getDoubleValue(jsonNode, "pH"),
                                getDoubleValue(jsonNode, "orp"),
                                getDoubleValue(jsonNode, "salinity"),
                                getDoubleValue(jsonNode, "water_level"),
                                getDoubleValue(jsonNode, "turbidity"),
                                getDoubleValue(jsonNode, "ammonia"),
                                getDoubleValue(jsonNode, "nitrites"),
                                LocalDateTime.now()
                        );

                        log.info("Parsed data: {}", data);

                        try {
                            simpMessagingTemplate.convertAndSend("/topic/water-quality", data);
                            log.info("Data sent to WebSocket clients");
                        } catch (Exception e) {
                            log.error("Failed to send data via WebSocket: {}", e.getMessage(), e);
                        }

                        try {
                            waterQualityServiceImpl.save(data);
                            log.info("Data saved to database");
                        } catch (Exception e) {
                            log.error("Failed to save data to database: {}", e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        log.error("Error processing data: {}", e.getMessage());
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    private Double getDoubleValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asDouble() : null;
    }

    public void startMonitoring() {
        try {
            if (!mqttClient.isConnected()) {
                connect();
            }

            mqttClient.subscribe(topic);
            isMonitoring.set(true);
            log.info("Started monitoring from topic: {}", topic);
        } catch (MqttException e) {
            log.error("Failed to start monitoring: {}", e.getMessage());
            throw new RuntimeException("Failed to start monitoring", e);
        }
    }

    public void stopMonitoring() {
        try {
            isMonitoring.set(false);
            mqttClient.unsubscribe(topic);
            log.info("Stopped monitoring");
        } catch (MqttException e) {
            log.error("Failed to stop monitoring: {}", e.getMessage());
            throw new RuntimeException("Failed to stop monitoring", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
                log.info("Disconnected from broker");
            }
        } catch (MqttException e) {
            log.error("Filed to disconnect from broker: {}", e.getMessage());
        }
    }

    public void sendTestMessage() {
        try {
            WaterQuality testData = new WaterQuality(
                    25.5, 85.0, 7.2, 250.0, 0.5, 120.0, 5.0, 0.02, 0.01, LocalDateTime.now()
            );

            log.info("Sending test data via WebSocket: {}", testData);
            simpMessagingTemplate.convertAndSend("/topic/water-quality", testData);
            log.info("Test data sent successfully");

            waterQualityServiceImpl.save(testData);
            log.info("Test data saved to database");
        } catch (Exception e) {
            log.error("Failed to send test message: {}", e.getMessage(), e);
        }
    }

}
