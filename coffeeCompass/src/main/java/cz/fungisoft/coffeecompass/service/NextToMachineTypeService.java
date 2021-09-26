package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;

public interface NextToMachineTypeService {

    NextToMachineType findNextToMachineTypeByName(String nextToMachineType);
    List<NextToMachineType> getAllNextToMachineTypes();
}
