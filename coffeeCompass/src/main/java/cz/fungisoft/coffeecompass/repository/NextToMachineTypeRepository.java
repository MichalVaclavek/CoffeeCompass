package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

public interface NextToMachineTypeRepository extends JpaRepository<NextToMachineType, UUID> {

    @Query("select ntmt from NextToMachineType ntmt where type=?1")
    NextToMachineType searchByName(String ntmType);
}
