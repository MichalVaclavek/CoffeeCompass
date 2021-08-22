package cz.fungisoft.coffeecompass.repository;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;

/**
 * For special Queries to get User entity results
 * 
 * @author Michal Vaclavek
 *
 */
public interface UsersRepositoryCustom {

    List<DBReturnPair> getTop5Users();
}
