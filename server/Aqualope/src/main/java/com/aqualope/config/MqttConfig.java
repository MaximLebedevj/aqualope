package com.aqualope.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MqttConfig {

    @Bean
    public ObjectMapper mqttObjectMapper() {
        return new ObjectMapper();
    }
}