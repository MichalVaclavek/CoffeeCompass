package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface SiteLocationTypeRepository extends JpaRepository<SiteLocationType, Integer> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select slt from SiteLocationType slt where locationType=?1")
    SiteLocationType searchByName(String locType);
}
