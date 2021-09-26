package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;

public interface CoffeeSortService {

    CoffeeSort findCoffeeSortByName(String coffeeSortName);
    List<CoffeeSort> getAllCoffeeSorts();
}
