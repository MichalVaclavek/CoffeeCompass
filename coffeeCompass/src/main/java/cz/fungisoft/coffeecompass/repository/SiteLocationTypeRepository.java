package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.SiteLocationType;

public interface SiteLocationTypeRepository extends JpaRepository<SiteLocationType, Integer>
{
    @Query("select slt from SiteLocationType slt where locationType=?1")
    public SiteLocationType searchByName(String locType);
}
