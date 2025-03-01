package com.aqualope.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TelegramNotificationService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void sendAlert(Long aquariumId, String parameter, Double value, Double lowerThreshold, Double upperThreshold) {
        String message = buildAlertMessage(aquariumId, parameter, value, lowerThreshold, upperThreshold);
        sendMessage(message);
    }

    private String buildAlertMessage(Long aquariumId, String parameter, Double value, Double lowerThreshold, Double upperThreshold) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ *WARNING* ⚠️\n\n");
        sb.append("*Aquarium ID:* ").append(aquariumId).append("\n");
        sb.append("*Parameter:* ").append(parameter).append("\n");
        sb.append("*Current Value:* ").append(String.format("%.2f", value)).append("\n");
        sb.append("*Acceptable Range:* ").append(String.format("%.2f", lowerThreshold))
                .append(" - ").append(String.format("%.2f", upperThreshold)).append("\n");
        sb.append("*Status:* Value is ");

        if (value < lowerThreshold) {
            sb.append("below minimum threshold");
        } else if (value > upperThreshold) {
            sb.append("above maximum threshold");
        }

        sb.append("\n*Time:* ").append(LocalDateTime.now().format(formatter));

        return sb.toString();
    }

    public void sendMessage(String text) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("chat_id", chatId);
            request.put("text", text);
            request.put("parse_mode", "Markdown");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent Telegram notification");
            } else {
                log.error("Failed to send Telegram notification. Response: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error sending Telegram notification: {}", e.getMessage(), e);
        }
    }
}