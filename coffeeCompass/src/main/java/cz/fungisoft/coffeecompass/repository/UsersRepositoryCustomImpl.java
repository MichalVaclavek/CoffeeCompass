package cz.fungisoft.coffeecompass.repository;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Repository;

import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;

/**
 * Gets special statistics or other info about Users.
 * 
 * @author Michal Vaclavek
 *
 */
@Repository
@Transactional
public class UsersRepositoryCustomImpl implements UsersRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public List<DBReturnPair> getTop5Users() {
       String selectQuery = "SELECT new cz.fungisoft.coffeecompass.entity.StatisticsToShow$DBReturnPair(u.userName, COUNT(cs))"
                          + " FROM CoffeeSite cs"
                          + " JOIN cs.originalUser u"
                          + " WHERE cs.recordStatus.status = :activeStatus"
                          + " GROUP BY u.userName"
                          + " ORDER BY COUNT(cs) DESC";

       TypedQuery<DBReturnPair> users = em.createQuery(selectQuery, DBReturnPair.class);
       users.setParameter("activeStatus", CoffeeSiteRecordStatusEnum.ACTIVE.getSiteRecordStatus());
       users.setMaxResults(5);

       return users.getResultList().stream()
                   .filter(rec -> !rec.getStrVal().isEmpty())
                   .toList();

    }
}
