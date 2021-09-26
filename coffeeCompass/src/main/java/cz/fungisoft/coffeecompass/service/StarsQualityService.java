package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;

public interface StarsQualityService {

    StarsQualityDescription findStarsQualityDescr(String starsQualityDescr);
    List<StarsQualityDescription> getAllStarsQualityDescriptions();
    StarsQualityDescription findStarsQualityById(Integer id);
}
