package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.PagingAndSortingRepository;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.User;

import jakarta.persistence.QueryHint;

import java.util.UUID;

/**
 * Interface to get Lists of found CoffeeSite in Pageable format. Extends Spring interface PagingAndSortingRepository.<br>
 * No implementation needed as already implemented by Spring.<br>
 * <p>
 * Used by {@link cz.fungisoft.coffeecompass.serviceimpl.CoffeeSiteServiceImpl}
 * 
 * @author Michal Vaclavek
 *
 */
public interface CoffeeSitePageableRepository extends PagingAndSortingRepository<CoffeeSite, UUID> {

    Page<CoffeeSite> findAll(Pageable pageable);

    Page<CoffeeSite> findByRecordStatus(CoffeeSiteRecordStatus recordStatus, Pageable pageable);

    Page<CoffeeSite> findByOriginalUser(User originalUser, Pageable pageable);

    /**
     * Same as previous method but filters out sites whose RecordStatus.status is not equal to given value.
     * In our case we use it to obtain NOT CANCELED sites for REST requests for user's sites.
     *
     * @param originalUser
     * @param status
     * @param pageable
     * @return
     */
//    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Page<CoffeeSite> findByOriginalUserAndRecordStatusStatusNot(User originalUser, String status, Pageable pageable);
}
