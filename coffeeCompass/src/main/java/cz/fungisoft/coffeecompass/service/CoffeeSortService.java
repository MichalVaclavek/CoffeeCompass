package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CoffeeSortDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;

public interface CoffeeSortService {

    Optional<CoffeeSort> findCoffeeSortById(UUID uuid);
    List<CoffeeSortDTO> getAllCoffeeSorts();

    Optional<CoffeeSort> searchByName(String cSort);

}
