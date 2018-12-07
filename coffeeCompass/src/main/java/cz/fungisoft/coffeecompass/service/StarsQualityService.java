package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;

public interface StarsQualityService
{
    public StarsQualityDescription findStarsQualityDescr(String starsQualityDescr);
    public List<StarsQualityDescription> getAllStarsQualityDescriptions();
    public StarsQualityDescription findStarsQualityById(Integer id);
}
