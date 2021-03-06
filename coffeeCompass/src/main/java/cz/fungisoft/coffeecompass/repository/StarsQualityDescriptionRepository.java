package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;

public interface StarsQualityDescriptionRepository extends JpaRepository<StarsQualityDescription, Integer>
{
    @Query("select sqd from StarsQualityDescription sqd where quality=?1")
    public StarsQualityDescription searchByName(String starsQDescr);
}
