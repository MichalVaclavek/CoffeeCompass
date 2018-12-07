package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;

public interface CoffeeSiteTypeRepository extends JpaRepository<CoffeeSiteType, Integer>
{
    @Query("select cst from CoffeeSiteType cst where coffeeSiteType=?1")
    public CoffeeSiteType searchByName(String siteType);
}
