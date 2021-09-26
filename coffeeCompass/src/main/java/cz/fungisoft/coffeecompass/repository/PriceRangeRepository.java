package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.PriceRange;

public interface PriceRangeRepository extends JpaRepository<PriceRange, Integer> {

    @Query("select pr from PriceRange pr where priceRange=?1")
    PriceRange searchByName(String range);
}
