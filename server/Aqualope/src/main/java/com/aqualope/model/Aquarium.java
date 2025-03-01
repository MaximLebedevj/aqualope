package com.aqualope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aquarium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double lowerThreshold = 0.0;
    private Double upperThreshold = 100.0;
    private String parameter;

    public Aquarium(Double lowerThreshold, Double upperThreshold, String parameter) {
        this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
        this.parameter = parameter;
    }
}