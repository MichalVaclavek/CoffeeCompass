package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.Optional;
import java.util.UUID;

public interface StarsQualityDescriptionRepository extends JpaRepository<StarsQualityDescription, UUID> {

    @Query("select sqd from StarsQualityDescription sqd where quality=?1")
    StarsQualityDescription searchByName(String starsQDescr);

    @Query("select sqd from StarsQualityDescription sqd where id=?1")
    Optional<StarsQualityDescription> searchById(Integer id);

    @Query("select sqd from StarsQualityDescription sqd where numOfStars=?1")
    Optional<StarsQualityDescription> searchByNumOfStars(Integer id);
}
