package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;

public interface NextToMachineTypeRepository extends JpaRepository<NextToMachineType, Integer>
{
    @Query("select ntmt from NextToMachineType ntmt where type=?1")
    public NextToMachineType searchByName(String ntmType);
}
