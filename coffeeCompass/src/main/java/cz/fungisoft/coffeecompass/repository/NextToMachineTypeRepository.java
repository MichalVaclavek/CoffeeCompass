package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface NextToMachineTypeRepository extends JpaRepository<NextToMachineType, Integer> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select ntmt from NextToMachineType ntmt where type=?1")
    NextToMachineType searchByName(String ntmType);
}
