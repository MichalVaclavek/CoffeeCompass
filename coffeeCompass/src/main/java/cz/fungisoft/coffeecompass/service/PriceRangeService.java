package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.PriceRangeDTO;
import cz.fungisoft.coffeecompass.entity.PriceRange;

public interface PriceRangeService {

    PriceRange findPriceRangeByString(String priceRange);
    PriceRange findPriceRangeByExtId(String extId);
    List<PriceRangeDTO> getAllPriceRanges();
}
