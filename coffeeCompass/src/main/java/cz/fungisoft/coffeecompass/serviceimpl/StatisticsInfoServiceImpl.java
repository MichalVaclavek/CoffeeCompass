package cz.fungisoft.coffeecompass.serviceimpl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.DiagnosticData;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;
import lombok.extern.log4j.Log4j2;

/**
 * Implements service to obtain needed statistical adn diagnostic info.
 * Uses Repositories for other Entities (or respective Services) to get required info about Entities.
 * 
 * @author Michal Vaclavek
 *
 */
@Service("statisticsInfoService")
@Transactional
@Log4j2
public class StatisticsInfoServiceImpl implements StatisticsInfoService
{
    private CoffeeSiteRepository csRepo;
    
    private UsersRepository usersRepo;

    /**
     * Object to be created, filled-in by respective data and returned on request.
     */
    private StatisticsToShow statsToShow;
    
    @Autowired
    public StatisticsInfoServiceImpl(CoffeeSiteRepository csRepo, UsersRepository usersRepo) {
        super();
        this.csRepo = csRepo;
        this.usersRepo = usersRepo;
        statsToShow = new StatisticsToShow();
    }

    @Override
    public StatisticsToShow getCurrentStatisticalInfoToShow() {
        statsToShow.setNumOfAllSites(csRepo.getNumOfAllActiveSites());
        statsToShow.setNumOfNewSitesToday(csRepo.getNumOfSitesCreatedToday());
        statsToShow.setNumOfNewSitesLast7Days(csRepo.getNumOfSitesCreatedLast7Days());
        
        statsToShow.setNumOfAllUsers(usersRepo.countAllUsers());
        statsToShow.setNumOfNewUsersThisWeek(usersRepo.getNumOfUsersRegisteredLast7Days());
        
        List<DBReturnPair> cities = csRepo.getTop5CityNames();
        statsToShow.setTop5CitiesMostCoffeeSites(cities);

        List<DBReturnPair> users = usersRepo.getTop5Users();
        statsToShow.setTop5UserNamesMostCreatedSites(users);
        
        return statsToShow;
    }

    @Override
    public DiagnosticData getCurrentDiagnosticData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveDiagnosticData(DiagnosticData diagData) {
        // TODO Auto-generated method stub

    }

}
