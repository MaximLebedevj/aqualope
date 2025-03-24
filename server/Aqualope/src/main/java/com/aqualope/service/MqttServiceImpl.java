package com.aqualope.service;

import com.aqualope.model.Aquarium;
import com.aqualope.model.WaterQuality;
import com.aqualope.websocket.WaterQualityWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mqtt.topic.arduino:topic/arduino_control}")
    private String arduinoTopic;

    private MqttClient mqttClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicBoolean isMonitoring = new AtomicBoolean(false);
    private final AtomicBoolean isAquaMonitoring = new AtomicBoolean(false);
    private Long currentAquariumId;

    @Autowired
    private WaterQualityWebSocketHandler websocketHandler;

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
                    getDoubleValue(jsonNode, "te"),
                    getDoubleValue(jsonNode, "ox"),
                    getDoubleValue(jsonNode, "p"),
                    getDoubleValue(jsonNode, "or"),
                    getDoubleValue(jsonNode, "s"),
                    getDoubleValue(jsonNode, "w"),
                    getDoubleValue(jsonNode, "tu"),
                    getDoubleValue(jsonNode, "a"),
                    getDoubleValue(jsonNode, "n"),
                    LocalDateTime.now()
            );

            log.info("Parsed data: {}", data);

            if (isMonitoring.get() || isAquaMonitoring.get()) {
                try {
                    WaterQuality savedData = waterQualityServiceImpl.save(data);
                    data.setId(savedData.getId());

                    websocketHandler.sendWaterQualityData(data);
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


                            /*
                            Telegram WS data
                             */

//                            Map<String, Object> parameterData = new HashMap<>();
//                            parameterData.put("aquariumId", aquarium.getId());
//                            parameterData.put("parameter", paramName);
//                            parameterData.put("value", parameterValue);
//                            parameterData.put("lowerThreshold", lowerThreshold);
//                            parameterData.put("upperThreshold", upperThreshold);
//                            parameterData.put("timestamp", data.getTimestamp());
//                            parameterData.put("isAlert", parameterValue < lowerThreshold || parameterValue > upperThreshold);

                            //websocketHandler.sendAquariumParameterData(parameterData);
                            //log.info("Parameter data sent to WebSocket clients: {}", parameterData);
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

    public void sendLedCommand(boolean turnOn) throws Exception {
        if (!isConnected()) {
            log.error("Cannot send command - MQTT client not connected");
            throw new Exception("MQTT client not connected");
        }

        try {
            Map<String, String> command = new HashMap<>();
            command.put("command", turnOn ? "led_on" : "led_off");

            String jsonCommand = objectMapper.writeValueAsString(command);
            log.info("Sending command to Arduino: {}", jsonCommand);

            MqttMessage message = new MqttMessage(jsonCommand.getBytes());
            message.setQos(1);
            mqttClient.publish(arduinoTopic, message);

            log.info("Command sent successfully");
        } catch (MqttException e) {
            log.error("MQTT error while sending command: {}", e.getMessage(), e);
            throw new Exception("Failed to send MQTT message: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error sending command: {}", e.getMessage(), e);
            throw new Exception("Failed to send command: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }

    public void sendArduinoCommand(String commandName, Map<String, Object> params) throws Exception {
        if (!isConnected()) {
            log.error("Cannot send command - MQTT client not connected");
            throw new Exception("MQTT client not connected");
        }

        try {
            Map<String, Object> commandData = new HashMap<>();
            commandData.put("command", commandName);

            if (params != null && !params.isEmpty()) {
                commandData.putAll(params);
            }

            String jsonCommand = objectMapper.writeValueAsString(commandData);
            log.info("Sending command to Arduino: {}", jsonCommand);

            MqttMessage message = new MqttMessage(jsonCommand.getBytes());
            message.setQos(1);
            mqttClient.publish(arduinoTopic, message);

            log.info("Command sent successfully");
        } catch (MqttException e) {
            log.error("MQTT error while sending command: {}", e.getMessage(), e);
            throw new Exception("Failed to send MQTT message: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error sending command: {}", e.getMessage(), e);
            throw new Exception("Failed to send command: " + e.getMessage());
        }
    }
}