package cz.fungisoft.users.repository;

import java.util.List;

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
       String selectQuery = "SELECT DISTINCT users.username, COUNT(*) as NumOfSites FROM coffeecompass.coffee_site AS cs"
                          + " JOIN coffeecompass.user AS users ON cs.zadal_user_id=users.id"
                          + " WHERE cs.status_zaznamu_id=1"
                          + " GROUP BY username"
                          + " ORDER BY NumOfSites DESC LIMIT 5";

       Query users = em.createNativeQuery(selectQuery);

       List<Object[]> results = users.getResultList();
      
       return results.stream().filter(rec -> !((String)rec[0]).isEmpty())
                     .map(rec -> new DBReturnPair((String)rec[0], (Long)rec[1]))
                     .toList();

    }
}
