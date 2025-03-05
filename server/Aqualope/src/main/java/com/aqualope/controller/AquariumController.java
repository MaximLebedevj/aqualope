package com.aqualope.controller;

import com.aqualope.model.Aquarium;
import com.aqualope.model.WaterQuality;
import com.aqualope.service.AquariumService;
import com.aqualope.service.WaterQualityServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/aquarium")
@Slf4j
public class AquariumController {

    @Autowired
    private AquariumService aquariumService;

    @Autowired
    private WaterQualityServiceImpl waterQualityService;

    @PostMapping
    public ResponseEntity<Aquarium> createAquarium(@RequestBody Map<String, Object> request) {
        log.info("Received request to create aquarium: {}", request);

        String name = (String) request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> parametersData = (List<Map<String, Object>>) request.get("parameters");
        if (parametersData == null || parametersData.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Aquarium.Parameter> parameters = new ArrayList<>();

        for (Map<String, Object> paramData : parametersData) {
            String paramName = (String) paramData.get("name");
            Double lowerThreshold = paramData.containsKey("lowerThreshold") ?
                    Double.parseDouble(paramData.get("lowerThreshold").toString()) : 0.0;
            Double upperThreshold = paramData.containsKey("upperThreshold") ?
                    Double.parseDouble(paramData.get("upperThreshold").toString()) : 100.0;

            if (paramName == null || paramName.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            parameters.add(new Aquarium.Parameter(paramName, lowerThreshold, upperThreshold));
        }

        Aquarium aquarium = aquariumService.createAquarium(name, parameters);
        return ResponseEntity.ok(aquarium);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAquariums() {
        log.info("Received request to get all aquariums");
        List<Aquarium> aquariums = aquariumService.getAllAquariums();

        List<Map<String, Object>> aquariumResponse = aquariums.stream().map(aquarium -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", aquarium.getId());
            response.put("name", aquarium.getName());
            response.put("parameters", aquarium.getParameters());
            return response;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(aquariumResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAquariumById(@PathVariable Long id) {
        log.info("Received request to get aquarium with id: {}", id);
        Optional<Aquarium> aquariumOpt = aquariumService.getAquariumById(id);

        return aquariumOpt.map(aquarium -> {
            Map<String, Object> response = new HashMap<>();
            response.put("id", aquarium.getId());
            response.put("name", aquarium.getName());
            response.put("parameters", aquarium.getParameters());
            return ResponseEntity.ok(response);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/water-quality")
    public ResponseEntity<List<WaterQuality>> getAquariumWaterQuality(@PathVariable Long id) {
        log.info("Received request to get water quality for aquarium id: {}", id);
        List<WaterQuality> waterQualities = waterQualityService.getLast24Hours();
        return ResponseEntity.ok(waterQualities);
    }

    @GetMapping("/{id}/params")
    public ResponseEntity<List<String>> getAquariumParameters(@PathVariable Long id) {
        log.info("Received request to get parameters for aquarium with id: {}", id);

        Optional<Aquarium> aquariumOpt = aquariumService.getAquariumById(id);

        return aquariumOpt.map(aquarium -> {
            List<String> parameterNames = aquarium.getParameters().stream()
                    .map(Aquarium.Parameter::getName)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(parameterNames);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}