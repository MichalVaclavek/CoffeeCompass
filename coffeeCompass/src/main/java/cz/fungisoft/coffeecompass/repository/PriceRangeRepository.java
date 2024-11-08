package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.PriceRange;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

public interface PriceRangeRepository extends JpaRepository<PriceRange, UUID> {

    @Query("select pr from PriceRange pr where priceRange=?1")
    PriceRange searchByName(String range);
}
