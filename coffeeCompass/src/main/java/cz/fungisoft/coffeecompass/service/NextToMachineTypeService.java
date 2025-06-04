package cz.fungisoft.coffeecompass.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.NextToMachineTypeDTO;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;

public interface NextToMachineTypeService {

    NextToMachineType findNextToMachineTypeByExtId(String extId);
    List<NextToMachineTypeDTO> getAllNextToMachineTypes();

    Optional<NextToMachineType> findById(UUID id);
}
