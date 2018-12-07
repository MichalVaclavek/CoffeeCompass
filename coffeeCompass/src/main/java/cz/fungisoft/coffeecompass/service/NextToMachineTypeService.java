package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;

public interface NextToMachineTypeService
{
    public NextToMachineType findNextToMachineTypeByName(String nextToMachineType);
    public List<NextToMachineType> getAllNextToMachineTypes();
}
