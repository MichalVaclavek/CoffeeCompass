package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteTypeDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;

public interface CoffeeSiteTypeService {

    CoffeeSiteType findCoffeeSiteTypeByName(String coffeeSiteType);
    List<CoffeeSiteTypeDTO> getAllCoffeeSiteTypes();
}
