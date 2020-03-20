package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Interface to get Lists of found CoffeeSite in Pageable format. Extends Spring interface PagingAndSortingRepository.<br>
 * No implementation needed as already implemented by Spring.<br>
 * <p>
 * Used by {@link CoffeeSiteServiceImpl}
 * 
 * @author Michal Vaclavek
 *
 */
public interface CoffeeSitePageableRepository extends PagingAndSortingRepository<CoffeeSite, Long>
{
    Page<CoffeeSite> findAll(Pageable pageable);
    Page<CoffeeSite> findByRecordStatus(CoffeeSiteRecordStatus recordStatus, Pageable pageable);
    Page<CoffeeSite> findByOriginalUser(User originalUser, Pageable pageable);
}
