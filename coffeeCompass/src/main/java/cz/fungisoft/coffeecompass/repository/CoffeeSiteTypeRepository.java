package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface CoffeeSiteTypeRepository extends JpaRepository<CoffeeSiteType, Integer> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select cst from CoffeeSiteType cst where coffeeSiteType=?1")
    CoffeeSiteType searchByName(String siteType);
}
