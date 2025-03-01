package com.aqualope.service;

import com.aqualope.model.Aquarium;
import com.aqualope.repository.AquariumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AquariumService {

    @Autowired
    private AquariumRepository aquariumRepository;

    public Aquarium createAquarium(Double lowerThreshold, Double upperThreshold, String parameter) {
        Aquarium aquarium = new Aquarium(lowerThreshold, upperThreshold, parameter);
        return aquariumRepository.save(aquarium);
    }

    public List<Aquarium> getAllAquariums() {
        return aquariumRepository.findAll();
    }

    public Optional<Aquarium> getAquariumById(Long id) {
        return aquariumRepository.findById(id);
    }

    public Aquarium updateThresholds(Long id, Double lowerThreshold, Double upperThreshold, String parameter) {
        Optional<Aquarium> aquariumOpt = aquariumRepository.findById(id);

        if (aquariumOpt.isPresent()) {
            Aquarium aquarium = aquariumOpt.get();
            aquarium.setLowerThreshold(lowerThreshold);
            aquarium.setUpperThreshold(upperThreshold);

            if (parameter != null && !parameter.isEmpty()) {
                aquarium.setParameter(parameter);
            }

            return aquariumRepository.save(aquarium);
        }

        throw new RuntimeException("Aquarium not found with id: " + id);
    }

}