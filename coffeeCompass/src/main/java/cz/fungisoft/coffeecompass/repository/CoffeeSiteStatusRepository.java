/**
 * 
 */
package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;

/**
 * @author Michal Vaclavek
 */
public interface CoffeeSiteStatusRepository extends JpaRepository<CoffeeSiteStatus, Integer>
{
    @Query("select css from CoffeeSiteStatus css where status=?1")
    CoffeeSiteStatus searchByName(String status);
}
