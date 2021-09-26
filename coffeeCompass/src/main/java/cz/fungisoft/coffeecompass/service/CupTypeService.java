package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.CupType;

public interface CupTypeService {

    CupType findCupTypeByName(String cupType);
    List<CupType> getAllCupTypes();
}
