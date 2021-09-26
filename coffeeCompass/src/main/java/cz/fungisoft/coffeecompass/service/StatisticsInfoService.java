package cz.fungisoft.coffeecompass.service;

import cz.fungisoft.coffeecompass.entity.DiagnosticData;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow;

/**
 * A service to obtain statistics info. Either to be shown to User or as diagnostic
 * data to analyse performance of the app.
 * 
 * @author Michal Vaclavek
 *
 */
public interface StatisticsInfoService {

    /**
     * Gets all current statistical data.
     * 
     * @return
     */
    StatisticsToShow getCurrentStatisticalInfoToShow();
    
    DiagnosticData getCurrentDiagnosticData();
    
    void saveDiagnosticData(DiagnosticData diagData);
}
