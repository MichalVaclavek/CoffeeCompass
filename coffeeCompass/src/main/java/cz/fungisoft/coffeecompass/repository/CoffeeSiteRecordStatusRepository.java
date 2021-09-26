package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;

public interface CoffeeSiteRecordStatusRepository extends JpaRepository<CoffeeSiteRecordStatus, Integer> {

    @Query("select csrs from CoffeeSiteRecordStatus csrs where status=?1")
    CoffeeSiteRecordStatus searchByName(String status);
}
