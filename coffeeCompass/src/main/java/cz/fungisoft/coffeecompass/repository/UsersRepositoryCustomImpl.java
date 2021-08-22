package cz.fungisoft.coffeecompass.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;

/**
 * Gets special statistics or other info abou Users.
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
       String selectQuery = "SELECT DISTINCT users.username, COUNT(*) as NumOfSites FROM coffeecompass.coffee_site AS cs"
                          + " JOIN coffeecompass.user AS users ON cs.zadal_user_id=users.id"
                          + " WHERE cs.status_zaznamu_id=1"
                          + " GROUP BY username"
                          + " ORDER BY NumOfSites DESC LIMIT 5";

       Query users = em.createNativeQuery(selectQuery);

       List<Object[]> results = users.getResultList();
      
       return results.stream().filter(record -> !((String)record[0]).isEmpty())
                     .map(record -> new DBReturnPair((String)record[0], (BigInteger)record[1]))
                     .collect(Collectors.toList());

    }
}
