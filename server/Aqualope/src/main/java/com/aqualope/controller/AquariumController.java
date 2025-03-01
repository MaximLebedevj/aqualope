package com.aqualope.controller;

import com.aqualope.model.Aquarium;
import com.aqualope.service.AquariumService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/aquarium")
@Slf4j
public class AquariumController {

    @Autowired
    private AquariumService aquariumService;

    @PostMapping
    public ResponseEntity<Aquarium> createAquarium(@RequestBody Map<String, Object> request) {
        log.info("Received request to create aquarium: {}", request);

        List<Map<String, Object>> parametersData = (List<Map<String, Object>>) request.get("parameters");
        if (parametersData == null || parametersData.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Aquarium.Parameter> parameters = new ArrayList<>();

        for (Map<String, Object> paramData : parametersData) {
            String name = (String) paramData.get("name");
            Double lowerThreshold = paramData.containsKey("lowerThreshold") ?
                    Double.parseDouble(paramData.get("lowerThreshold").toString()) : 0.0;
            Double upperThreshold = paramData.containsKey("upperThreshold") ?
                    Double.parseDouble(paramData.get("upperThreshold").toString()) : 100.0;

            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            parameters.add(new Aquarium.Parameter(name, lowerThreshold, upperThreshold));
        }

        Aquarium aquarium = aquariumService.createAquarium(parameters);
        return ResponseEntity.ok(aquarium);
    }

    @GetMapping
    public ResponseEntity<List<Aquarium>> getAllAquariums() {
        log.info("Received request to get all aquariums");
        List<Aquarium> aquariums = aquariumService.getAllAquariums();
        return ResponseEntity.ok(aquariums);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Aquarium> getAquariumById(@PathVariable Long id) {
        log.info("Received request to get aquarium with id: {}", id);
        Optional<Aquarium> aquarium = aquariumService.getAquariumById(id);
        return aquarium.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Aquarium> updateAquarium(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("Received request to update aquarium with id: {}", id);

        List<Map<String, Object>> parametersData = (List<Map<String, Object>>) request.get("parameters");
        if (parametersData == null || parametersData.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Aquarium.Parameter> parameters = new ArrayList<>();

        for (Map<String, Object> paramData : parametersData) {
            String name = (String) paramData.get("name");
            Double lowerThreshold = paramData.containsKey("lowerThreshold") ?
                    Double.parseDouble(paramData.get("lowerThreshold").toString()) : 0.0;
            Double upperThreshold = paramData.containsKey("upperThreshold") ?
                    Double.parseDouble(paramData.get("upperThreshold").toString()) : 100.0;

            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            parameters.add(new Aquarium.Parameter(name, lowerThreshold, upperThreshold));
        }

        try {
            Aquarium aquarium = aquariumService.updateParameters(id, parameters);
            return ResponseEntity.ok(aquarium);
        } catch (RuntimeException e) {
            log.error("Failed to update aquarium: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}