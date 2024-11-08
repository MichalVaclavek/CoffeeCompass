/**
 * 
 */
package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

/**
 * @author Michal Vaclavek
 */
public interface CoffeeSiteStatusRepository extends JpaRepository<CoffeeSiteStatus, UUID> {

    @Query("select css from CoffeeSiteStatus css where status=?1")
    CoffeeSiteStatus searchByName(String status);
}
