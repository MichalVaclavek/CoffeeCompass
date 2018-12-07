/**
 * 
 */
package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSort;

/**
 * @author Michal Vaclavek
 */
public interface CoffeeSortRepository extends JpaRepository<CoffeeSort, Integer>
{
    @Query("select csort from CoffeeSort csort where coffeeSort=?1")
    public CoffeeSort searchByName(String coffeeSort);
}
