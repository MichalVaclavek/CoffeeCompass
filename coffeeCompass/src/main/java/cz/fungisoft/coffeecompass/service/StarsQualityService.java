package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.StarsQualityDescriptionDTO;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;

public interface StarsQualityService {

    StarsQualityDescription findStarsQualityDescr(String starsQualityDescr);
    List<StarsQualityDescriptionDTO> getAllStarsQualityDescriptions();
    StarsQualityDescription findStarsQualityById(Integer id);
    StarsQualityDescription findStarsQualityByExtId(String extId);
}
