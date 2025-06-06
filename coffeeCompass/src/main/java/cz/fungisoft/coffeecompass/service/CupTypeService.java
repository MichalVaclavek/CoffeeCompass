package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.CupTypeDTO;
import cz.fungisoft.coffeecompass.entity.CupType;

public interface CupTypeService {

    CupType findCupTypeByExtId(String cupType);
    List<CupTypeDTO> getAllCupTypes();
    Optional<CupType> findCupTypeById(UUID id);
}
