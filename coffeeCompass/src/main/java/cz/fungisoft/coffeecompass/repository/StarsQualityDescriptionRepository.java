package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface StarsQualityDescriptionRepository extends JpaRepository<StarsQualityDescription, Integer> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select sqd from StarsQualityDescription sqd where quality=?1")
    StarsQualityDescription searchByName(String starsQDescr);
}
