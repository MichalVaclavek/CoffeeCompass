package cz.fungisoft.coffeecompass.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class StatisticsTest {
    
    @Autowired
    private CoffeeSiteRepository csRepo;
    
    @Autowired
    private UsersRepository usersRepo;

    private StatisticsToShow statsToShow;
    
    @Test
    void testStatistics() {
        statsToShow = new StatisticsToShow();
        
        statsToShow.setNumOfAllSites(csRepo.getNumOfAllActiveSites());
        statsToShow.setNumOfNewSitesToday(csRepo.getNumOfSitesCreatedToday());
        statsToShow.setNumOfNewSitesLast7Days(csRepo.getNumOfSitesCreatedLast7Days());
        
        List<DBReturnPair> cities = csRepo.getTop5CityNames();
        statsToShow.setTop5CitiesMostCoffeeSites(cities);
        assertThat(cities.size()).isPositive();
        
        List<DBReturnPair> users = usersRepo.getTop5Users();
        statsToShow.setTop5UserNamesMostCreatedSites(users);
        assertThat(users.size()).isPositive();
    }
}
