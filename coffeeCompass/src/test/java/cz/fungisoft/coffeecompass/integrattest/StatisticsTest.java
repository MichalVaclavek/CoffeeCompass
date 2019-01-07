package cz.fungisoft.coffeecompass.integrattest;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cz.fungisoft.coffeecompass.CoffeeCompassApplication;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import lombok.extern.log4j.Log4j2;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CoffeeCompassApplication.class})
@AutoConfigureMockMvc
@Log4j2
public class StatisticsTest
{
    @Autowired
    private CoffeeSiteRepository csRepo;
    
    @Autowired
    private UsersRepository usersRepo;

    private StatisticsToShow statsToShow;
    
    @Test
    public void testStatistics() {
        statsToShow = new StatisticsToShow();
        
        statsToShow.setNumOfAllSites(csRepo.getNumOfAllActiveSites());
        statsToShow.setNumOfNewSitesToday(csRepo.getNumOfSitesCreatedToday());
        statsToShow.setNumOfNewSitesLast7Days(csRepo.getNumOfSitesCreatedLast7Days());
        
        List<DBReturnPair> cities = csRepo.getTop5CityNames();
        statsToShow.setTop5CitiesMostCoffeeSites(cities);
        assertThat(cities.size()).isGreaterThan(0);
        
        List<DBReturnPair> users = usersRepo.getTop5Users();
        statsToShow.setTop5UserNamesMostCreatedSites(users);
        assertThat(users.size()).isGreaterThan(0);
    }
}
