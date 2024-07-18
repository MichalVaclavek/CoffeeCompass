/**
 * 
 */
package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

/**
 * @author Michal Vaclavek
 */
public interface CoffeeSortRepository extends JpaRepository<CoffeeSort, Integer> {

//    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select csort from CoffeeSort csort where coffeeSort=?1")
    CoffeeSort searchByName(String coffeeSort);
}
