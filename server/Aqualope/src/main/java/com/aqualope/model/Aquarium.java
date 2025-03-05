package com.aqualope.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aquarium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aquarium_parameters", joinColumns = @JoinColumn(name = "aquarium_id"))
    private List<Parameter> parameters = new ArrayList<>();

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String name;
        private Double lowerThreshold = 0.0;
        private Double upperThreshold = 100.0;
    }

    public Aquarium(String name, List<Parameter> parameters) {
        this.name = name;
        this.parameters = parameters;
    }
}