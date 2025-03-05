package com.aqualope.service;

import com.aqualope.model.WaterQuality;
import com.aqualope.repository.WaterQualityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class WaterQualityServiceImpl {

    @Autowired
    private WaterQualityRepository waterQualityRepository;

    public WaterQuality save(WaterQuality waterQuality) {
        return waterQualityRepository.save(waterQuality);
    }

    public List<WaterQuality> getLast24Hours() {
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(24);
        return waterQualityRepository.getWaterQualityLastDay(dayAgo);
    }

    @Scheduled(fixedRate = 3600000)
    public void cleanupOldData() {
        LocalDateTime dayAgo = LocalDateTime.now().minusHours(24);
        List<WaterQuality> allData = waterQualityRepository.findAll();

        allData.stream()
                .filter(data -> data.getTimestamp().isBefore(dayAgo))
                .forEach(waterQualityRepository::delete);
    }
}
