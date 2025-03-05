package com.aqualope.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double temperature;
    private Double oxygenSaturation;
    private Double pH;
    private Double orp;
    private Double salinity;
    private Double waterLevel;
    private Double turbidity;
    private Double ammonia;
    private Double nitrites;
    private LocalDateTime timestamp;

    public WaterQuality(Double temperature, Double oxygenSaturation, Double pH,
                        Double orp, Double salinity, Double waterLevel, Double turbidity,
                        Double ammonia, Double nitrites, LocalDateTime timestamp) {
        this.temperature = temperature;
        this.oxygenSaturation = oxygenSaturation;
        this.pH = pH;
        this.orp = orp;
        this.salinity = salinity;
        this.waterLevel = waterLevel;
        this.turbidity = turbidity;
        this.ammonia = ammonia;
        this.nitrites = nitrites;
        this.timestamp = timestamp;
    }
}