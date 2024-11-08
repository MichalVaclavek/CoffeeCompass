package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CupType;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

public interface CupTypeRepository extends JpaRepository<CupType, UUID> {

    @Query("select ct from CupType ct where cupType=?1")
    CupType searchByName(String cupType);
}
