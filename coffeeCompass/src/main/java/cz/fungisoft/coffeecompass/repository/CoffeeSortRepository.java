/**
 * 
 */
package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.UUID;

/**
 * @author Michal Vaclavek
 */
public interface CoffeeSortRepository extends JpaRepository<CoffeeSort, UUID> {

    @Query("select csort from CoffeeSort csort where coffeeSort=?1")
    CoffeeSort searchByName(String coffeeSort);
}
