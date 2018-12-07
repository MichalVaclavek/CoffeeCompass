package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;

public interface CSTypeService
{
    public CoffeeSiteType findCoffeeSiteTypeByName(String csType);
    public List<CoffeeSiteType> getAllCoffeeSiteTypes();
}
