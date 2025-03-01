package com.aqualope.service;

import com.aqualope.model.Aquarium;
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
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;
import java.util.List;

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
    private final AtomicBoolean isAquaMonitoring = new AtomicBoolean(false);
    private Long currentAquariumId;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private WaterQualityServiceImpl waterQualityServiceImpl;

    @Autowired
    private AquariumService aquariumService;

    @Autowired
    private TelegramNotificationService telegramService;

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
                        if(isMonitoring.get() || isAquaMonitoring.get()) {
                            mqttClient.subscribe(topic);
                        }
                    }
                } catch (MqttException e) {
                    log.error("Failed to reconnect to broker: {}", e.getMessage());
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                handleMessage(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    private void handleMessage(MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.info("Received message: {}", payload);

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

            if (isMonitoring.get() || isAquaMonitoring.get()) {
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
            }

            if (isAquaMonitoring.get() && currentAquariumId != null) {
                Optional<Aquarium> aquariumOpt = aquariumService.getAquariumById(currentAquariumId);
                if (aquariumOpt.isPresent()) {
                    Aquarium aquarium = aquariumOpt.get();
                    List<Aquarium.Parameter> parameters = aquarium.getParameters();

                    for (Aquarium.Parameter parameter : parameters) {
                        String paramName = parameter.getName();
                        Double parameterValue = getParameterValue(data, paramName);

                        if (parameterValue != null) {
                            Double lowerThreshold = parameter.getLowerThreshold();
                            Double upperThreshold = parameter.getUpperThreshold();

                            if (parameterValue < lowerThreshold || parameterValue > upperThreshold) {
                                log.warn("Parameter {} value {} is outside acceptable range [{} - {}]",
                                        paramName, parameterValue, lowerThreshold, upperThreshold);

                                telegramService.sendAlert(
                                        aquarium.getId(),
                                        paramName,
                                        parameterValue,
                                        lowerThreshold,
                                        upperThreshold
                                );
                            }

                            Map<String, Object> parameterData = new HashMap<>();
                            parameterData.put("aquariumId", aquarium.getId());
                            parameterData.put("parameter", paramName);
                            parameterData.put("value", parameterValue);
                            parameterData.put("lowerThreshold", lowerThreshold);
                            parameterData.put("upperThreshold", upperThreshold);
                            parameterData.put("timestamp", data.getTimestamp());
                            parameterData.put("isAlert", parameterValue < lowerThreshold || parameterValue > upperThreshold);

                            simpMessagingTemplate.convertAndSend("/topic/aquarium-parameter", parameterData);
                            log.info("Parameter data sent to WebSocket clients: {}", parameterData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing data: {}", e.getMessage(), e);
        }
    }
    
    private Double getParameterValue(WaterQuality data, String parameter) {
        switch (parameter.toLowerCase()) {
            case "temperature":
            case "temp":
                return data.getTemperature();
            case "oxygensaturation":
            case "oxygen":
                return data.getOxygenSaturation();
            case "ph":
                return data.getPH();
            case "orp":
                return data.getOrp();
            case "salinity":
                return data.getSalinity();
            case "waterlevel":
            case "level":
                return data.getWaterLevel();
            case "turbidity":
                return data.getTurbidity();
            case "ammonia":
                return data.getAmmonia();
            case "nitrites":
                return data.getNitrites();
            default:
                log.warn("Unknown parameter: {}", parameter);
                return null;
        }
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
            if (!isAquaMonitoring.get()) {
                mqttClient.unsubscribe(topic);
            }
            log.info("Stopped regular monitoring");
        } catch (MqttException e) {
            log.error("Failed to stop monitoring: {}", e.getMessage());
            throw new RuntimeException("Failed to stop monitoring", e);
        }
    }

    public void startAquaMonitoring(Long aquariumId) {
        try {
            if (!mqttClient.isConnected()) {
                connect();
            }

            Optional<Aquarium> aquariumOpt = aquariumService.getAquariumById(aquariumId);
            if (!aquariumOpt.isPresent()) {
                throw new RuntimeException("Aquarium not found with id: " + aquariumId);
            }

            Aquarium aquarium = aquariumOpt.get();
            if (aquarium.getParameters() == null || aquarium.getParameters().isEmpty()) {
                throw new RuntimeException("Aquarium has no parameters configured");
            }

            currentAquariumId = aquariumId;

            if (!isMonitoring.get() && !isAquaMonitoring.get()) {
                mqttClient.subscribe(topic);
            }

            isAquaMonitoring.set(true);
            log.info("Started aquarium monitoring for aquarium id: {} with parameters: {}",
                    aquariumId, aquarium.getParameters());
        } catch (MqttException e) {
            log.error("Failed to start aquarium monitoring: {}", e.getMessage());
            throw new RuntimeException("Failed to start aquarium monitoring", e);
        }
    }

    public void stopAquaMonitoring() {
        try {
            isAquaMonitoring.set(false);
            currentAquariumId = null;

            if (!isMonitoring.get()) {
                mqttClient.unsubscribe(topic);
            }

            log.info("Stopped aquarium monitoring");
        } catch (MqttException e) {
            log.error("Failed to stop aquarium monitoring: {}", e.getMessage());
            throw new RuntimeException("Failed to stop aquarium monitoring", e);
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

            if (isAquaMonitoring.get() && currentAquariumId != null) {
                Optional<Aquarium> aquariumOpt = aquariumService.getAquariumById(currentAquariumId);
                if (aquariumOpt.isPresent()) {
                    Aquarium aquarium = aquariumOpt.get();
                    for (Aquarium.Parameter parameter : aquarium.getParameters()) {
                        String paramName = parameter.getName();
                        Double parameterValue = getParameterValue(testData, paramName);

                        if (parameterValue != null) {
                            Map<String, Object> parameterData = new HashMap<>();
                            parameterData.put("aquariumId", aquarium.getId());
                            parameterData.put("parameter", paramName);
                            parameterData.put("value", parameterValue);
                            parameterData.put("lowerThreshold", parameter.getLowerThreshold());
                            parameterData.put("upperThreshold", parameter.getUpperThreshold());
                            parameterData.put("timestamp", testData.getTimestamp());

                            simpMessagingTemplate.convertAndSend("/topic/aquarium-parameter", parameterData);
                            log.info("Test parameter data sent to WebSocket clients: {}", parameterData);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to send test message: {}", e.getMessage(), e);
        }
    }
}