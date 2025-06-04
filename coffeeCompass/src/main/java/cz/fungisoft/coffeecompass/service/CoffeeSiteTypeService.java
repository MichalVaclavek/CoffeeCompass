package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteTypeDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;

public interface CoffeeSiteTypeService {

    CoffeeSiteType findCoffeeSiteTypeByName(String coffeeSiteType);
    Optional<CoffeeSiteType> findById(UUID uuid);
    List<CoffeeSiteTypeDTO> getAllCoffeeSiteTypes();
}
