package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;

public interface CoffeeSiteTypeService
{
    public CoffeeSiteType findCoffeeSiteTypeByName(String coffeeSiteType);
    public List<CoffeeSiteType> getAllCoffeeSiteTypes();
}
