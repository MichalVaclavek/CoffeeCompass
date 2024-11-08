package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

public interface SiteLocationTypeRepository extends JpaRepository<SiteLocationType, UUID> {

    @Query("select slt from SiteLocationType slt where locationType=?1")
    SiteLocationType searchByName(String locType);
}
