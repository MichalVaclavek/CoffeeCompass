package cz.fungisoft.coffeecompass.testutils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Configuration;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.CupType.CupTypeEnum;
import cz.fungisoft.coffeecompass.entity.NextToMachineType.NextToMachineTypeEnum;

/**
 * Pomocna trida pro vytvareni novych instanci CoffeeSite s defaultnimi hodnotami jeho atributu.
 * Pro testovaci ucely, zvlastne pro Integracni testy.
 * 
 * @author Michal Vaclavek
 *
 */
@Configuration
public class CoffeeSiteFactory {
    /**
     * Creates and returns new CoffeeSite instance with some default attribute values.
     * 
     * @param siteName
     * @param coffeeSiteType
     * @return
     */
    public static CoffeeSite getCoffeeSite(String siteName, String coffeeSiteType) {
        
        Set<CoffeeSort> csorts = new HashSet<>();
        Set<CupType> cups = new HashSet<>();
        Company comp = new Company();
        StarsQualityDescription stars = new StarsQualityDescription();
        Set<NextToMachineType> ntmtSet = new HashSet<>(); 
        Set<OtherOffer> nabidka = new HashSet<>();
        SiteLocationType nadr = new SiteLocationType();
        User origUser = new User();
        CoffeeSiteStatus siteStatus = new CoffeeSiteStatus();
        CoffeeSiteType siteType = new CoffeeSiteType();
        siteType.setCoffeeSiteType(coffeeSiteType);
        CoffeeSiteRecordStatus recordStatus = new CoffeeSiteRecordStatus();
        
        PriceRange pr = new PriceRange();
        pr.setPriceRange("15 - 25 Kč");

        CoffeeSort cs = new CoffeeSort();
        cs.setCoffeeSort("Instantní");       
        csorts.add(cs);        
          
        CupType paper = new CupType();
        paper.setCupType(CupTypeEnum.PAPER.getCupType());       
        CupType plastic = new CupType();
        plastic.setCupType(CupTypeEnum.PLASTIC.getCupType());       
        cups.add(paper);
        cups.add(plastic);
          
        comp.setNameOfCompany("Kávička s.r.o");
          
        NextToMachineType mt = new NextToMachineType();
        NextToMachineType mt2 = new NextToMachineType();
        mt.setType(NextToMachineTypeEnum.NAPOJE.getNexToMachineType());
        mt2.setType(NextToMachineTypeEnum.BAGETY.getNexToMachineType());
          
        ntmtSet.add(mt);
        ntmtSet.add(mt2);
         
        OtherOffer caj = new OtherOffer();
        caj.setOffer("čaj");
        OtherOffer kafe = new OtherOffer();
        kafe.setOffer("káva");
                        
        nabidka.add(caj);
        nabidka.add(kafe);
          
        nadr.setLocationType("nádraží");
          
        UserProfile userProfUser = new UserProfile();
        userProfUser.setType("USER");
          
        origUser.setUserName("kava");
        origUser.setFirstName("Pan");
        origUser.setLastName("Tchibo");
          
        String emailAddr = "kava@tchibo.de";
        origUser.setEmail(emailAddr);
        origUser.setPassword("kofein");
        origUser.setCreatedOn(LocalDateTime.now());
          
        Set<UserProfile> userProfiles = new HashSet<>();
        userProfiles.add(userProfUser);
        origUser.setUserProfiles(userProfiles);   
          
        siteStatus.setStatus(CoffeeSiteStatusEnum.INSERVICE.getSiteStatus());     
        
        CoffeeSite coffeeS = new CoffeeSite();        
        
        coffeeS.setSiteName(siteName);
        coffeeS.setTypPodniku(siteType);
        coffeeS.setStatusZarizeni(siteStatus);
        coffeeS.setCena(pr);
        coffeeS.setCoffeeSorts(csorts);
        coffeeS.setCupTypes(cups);
        coffeeS.setInitialComment("Ujde");
        coffeeS.setTypPodniku(siteType);
        coffeeS.setNextToMachineTypes(ntmtSet);
        coffeeS.setOtherOffers(nabidka);
        coffeeS.setOriginalUser(origUser);
        coffeeS.setDodavatelPodnik(comp);
        coffeeS.setTypLokality(nadr);
        
        coffeeS.setRecordStatus(recordStatus);
        coffeeS.setCreatedOn(LocalDateTime.now());
        
        coffeeS.setMesto("Praha");
        coffeeS.setUliceCP("Wilssonova");
        
        coffeeS.setNumOfCoffeeAutomatyVedleSebe(1);
        
        coffeeS.setPristupnostDny("Po-Ne");      
        coffeeS.setPristupnostHod("00-24");
        
        coffeeS.setZemDelka(14.434331);
        coffeeS.setZemSirka(50.083484);
        
        return coffeeS;
    }
}
