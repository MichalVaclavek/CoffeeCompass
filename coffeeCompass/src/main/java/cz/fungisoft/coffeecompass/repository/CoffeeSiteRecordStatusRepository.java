package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

public interface CoffeeSiteRecordStatusRepository extends JpaRepository<CoffeeSiteRecordStatus, Integer> {

//    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select csrs from CoffeeSiteRecordStatus csrs where status=?1")
    CoffeeSiteRecordStatus searchByName(String status);
}
