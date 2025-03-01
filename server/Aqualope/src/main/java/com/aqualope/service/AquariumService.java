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

    public Aquarium createAquarium(List<Aquarium.Parameter> parameters) {
        Aquarium aquarium = new Aquarium(parameters);
        return aquariumRepository.save(aquarium);
    }

    public List<Aquarium> getAllAquariums() {
        return aquariumRepository.findAll();
    }

    public Optional<Aquarium> getAquariumById(Long id) {
        return aquariumRepository.findById(id);
    }

    public Aquarium updateParameters(Long id, List<Aquarium.Parameter> parameters) {
        Optional<Aquarium> aquariumOpt = aquariumRepository.findById(id);

        if (aquariumOpt.isPresent()) {
            Aquarium aquarium = aquariumOpt.get();
            aquarium.setParameters(parameters);
            return aquariumRepository.save(aquarium);
        }

        throw new RuntimeException("Aquarium not found with id: " + id);
    }
}