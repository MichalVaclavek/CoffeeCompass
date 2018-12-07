package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CupType;

public interface CupTypeService
{
    public CupType findCupTypeByName(String cupType);
    public List<CupType> getAllCupTypes();
}
