package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.PriceRange;

public interface PriceRangeService
{
    public PriceRange findPriceRangeByString(String priceRange);
    public List<PriceRange> getAllPriceRanges();
}
