package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;

public interface CoffeeSortService
{
    public CoffeeSort findCoffeeSortByName(String coffeeSortName);
    public List<CoffeeSort> getAllCoffeeSorts();
}
