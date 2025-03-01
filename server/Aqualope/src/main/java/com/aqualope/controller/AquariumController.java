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

@RestController
@RequestMapping("/api/aquarium")
@Slf4j
public class AquariumController {

    @Autowired
    private AquariumService aquariumService;

    @PostMapping
    public ResponseEntity<Aquarium> createAquarium(@RequestBody Map<String, Object> request) {
        log.info("Received request to create aquarium: {}", request);

        Double lowerThreshold = request.containsKey("lowerThreshold") ?
                Double.parseDouble(request.get("lowerThreshold").toString()) : 0.0;
        Double upperThreshold = request.containsKey("upperThreshold") ?
                Double.parseDouble(request.get("upperThreshold").toString()) : 100.0;
        String parameter = (String) request.get("parameter");

        if (parameter == null || parameter.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Aquarium aquarium = aquariumService.createAquarium(lowerThreshold, upperThreshold, parameter);
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

        Double lowerThreshold = request.containsKey("lowerThreshold")
                ? Double.parseDouble(request.get("lowerThreshold").toString())
                : 0.0;
        Double upperThreshold = request.containsKey("upperThreshold")
                ? Double.parseDouble(request.get("upperThreshold").toString())
                : 100.0;
        String parameter = request.containsKey("parameter")
                ? request.get("parameter").toString()
                : null;

        try {
            Aquarium aquarium = aquariumService.updateThresholds(id, lowerThreshold, upperThreshold, parameter);
            return ResponseEntity.ok(aquarium);
        } catch (RuntimeException e) {
            log.error("Failed to update aquarium: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}