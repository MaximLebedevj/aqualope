package com.aqualope.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class WaterQualityWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public WaterQualityWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("New WebSocket connection established: {}", session.getId());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage(), exception);
        sessions.remove(session);
    }

    public void sendWaterQualityData(Object data) {
        sendToAll(data, "water-quality");
    }

    public void sendAquariumParameterData(Object data) {
        sendToAll(data, "aquarium-parameter");
    }

    private void sendToAll(Object data, String messageType) {
        try {
            Map<String, Object> message = Map.of(
                    "type", messageType,
                    "data", data
            );

            String jsonMessage = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(jsonMessage);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        log.debug("Sending {} data to session {}: {}", messageType, session.getId(), jsonMessage);
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize or send message: {}", e.getMessage(), e);
        }
    }
}