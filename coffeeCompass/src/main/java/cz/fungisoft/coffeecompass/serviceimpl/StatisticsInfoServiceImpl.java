package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.DiagnosticData;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow.DBReturnPair;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements service to obtain needed statistical adn diagnostic info.
 * Uses Repositories for other Entities (or respective Services) to get required info about Entities.
 * 
 * @author Michal Vaclavek
 *
 */
@Service("statisticsInfoService")
@Transactional
@Slf4j
public class StatisticsInfoServiceImpl implements StatisticsInfoService {

    private final CoffeeSiteRepository csRepo;
    
    private final UsersRepository usersRepo;

    /**
     * Object to be created, filled-in by respective data and returned on request.
     */
    private final StatisticsToShow statsToShow;
    
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
        statsToShow.setNumOfNewSitesToday(csRepo.getNumOfSitesCreatedAndActiveToday());
        statsToShow.setNumOfNewSitesLast7Days(csRepo.getNumOfSitesCreatedAndActiveInLast7Days());
        
        statsToShow.setNumOfAllUsers(usersRepo.countAllEnabledUsers());
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
