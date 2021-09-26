package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;

public interface CSTypeService {

    CoffeeSiteType findCoffeeSiteTypeByName(String csType);
    List<CoffeeSiteType> getAllCoffeeSiteTypes();
}
