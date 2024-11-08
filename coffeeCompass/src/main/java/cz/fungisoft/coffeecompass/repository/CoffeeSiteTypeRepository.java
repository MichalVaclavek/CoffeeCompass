package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

public interface CoffeeSiteTypeRepository extends JpaRepository<CoffeeSiteType, UUID> {

    @Query("select cst from CoffeeSiteType cst where coffeeSiteType=?1")
    CoffeeSiteType searchByName(String siteType);
}
