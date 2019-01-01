package cz.fungisoft.coffeecompass.entity;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;

/**
 * Class to hold some interesting statistical information about main Entities<br>
 * to be shown to user.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class StatisticsToShow
{
    /**
     * Inner class to hold some of the statistics data pairs returned from DB request.
     */
    public static class DBReturnPair {
        
        private String strVal;
        private BigInteger intVal;

        
        public DBReturnPair(String strVal, BigInteger intVal) {
            super();
            this.strVal = strVal;
            this.intVal = intVal;
        }

        public String getStrVal() {
            return strVal;
        }

        public BigInteger getIntVal() {
            return intVal;
        }

        @Override
        public String toString() {
            return strVal + " (" + intVal + ")";
        }
    }
    
    /**
     * Number of all ACTIVE CoffeeSites in DB
     */
    private Long numOfAllSites = 0L;
    /**
     * Number of all CoffeeSites in DB created today (can be in any state currently)
     */
    private Long numOfNewSitesToday = 0L;
    /**
     * Number of all CoffeeSites in DB created within last 7 days (can be in any state currently)
     */
    private Long numOfNewSitesLast7Days = 0L;
    
    /**
     * Number of all Users in DB
     */
    private Long numOfAllUsers = 0L;
    /**
     * Number of all Users in DB registered in last 7 days.
     */
    private Long numOfNewUsersThisWeek = 0L;
    
    /**
     * List of 5 cities where are the highest number of ACTIVE Coffee sites and the number of sites.
     */
    private List<DBReturnPair> top5CitiesMostCoffeeSites;
    
    /**
     * List of 5 Users who created the most of Coffee sites and the number of sites created
     */
    private List<DBReturnPair> top5UserNamesMostCreatedSites; 
    
}
