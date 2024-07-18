package cz.fungisoft.users.repository;


/**
 * For special Queries to get User entity results
 * 
 * @author Michal Vaclavek
 *
 */
public interface UsersRepositoryCustom {

    List<DBReturnPair> getTop5Users();
}
