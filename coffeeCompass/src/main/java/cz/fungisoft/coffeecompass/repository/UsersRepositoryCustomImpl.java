package cz.fungisoft.coffeecompass.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
public class UsersRepositoryCustomImpl implements UsersRepositoryCustom
{
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

       List<DBReturnPair> retVal = new ArrayList<>();
      
       List<Object[]> results = users.getResultList();
      
       results.stream().filter(record -> !((String)record[0]).isEmpty())
                      .forEach(record -> { retVal.add(new DBReturnPair((String)record[0], (BigInteger)record[1])); });

       return retVal;

    }

}
