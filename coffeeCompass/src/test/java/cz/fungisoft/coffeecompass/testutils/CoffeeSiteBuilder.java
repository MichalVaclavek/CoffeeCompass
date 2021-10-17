/**
 * 
 */
package cz.fungisoft.coffeecompass.testutils;

import java.util.HashSet;
import java.util.Set;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus.CoffeeSiteStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.CupType.CupTypeEnum;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType.NextToMachineTypeEnum;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Vytvářeč instancí CoffeeSite, zvláště pro účely testování.
 * 
 * @author Michal Vaclavek
 */
public class CoffeeSiteBuilder {

    private CoffeeSite coffeeSite;
    
    /**
     * Konstruktor provede nektere zakladni nastaveni vlastnosti CoffeeSitu pomoci CoffeeSiteFactory
     * Ostatni se budou nastavovat builder metodami ????
     */
    public CoffeeSiteBuilder() {         
        coffeeSite = CoffeeSiteFactory.getCoffeeSite("Test", "automat");
    }
    
    public CoffeeSiteBuilder setName(String siteName) {
        coffeeSite.setSiteName(siteName);
        return this;
    }
    
    public CoffeeSiteBuilder setHodnoceni(String hodnoceni) {
        coffeeSite.setInitialComment(hodnoceni);
        return this;
    }
    
    public CoffeeSiteBuilder setStatusSitu(CoffeeSiteStatus siteStatus) {
        coffeeSite.setStatusZarizeni(siteStatus);
        return this;
    }
    
    public CoffeeSiteBuilder setStatusSitu(CoffeeSiteStatusEnum siteStatusEn) {
        CoffeeSiteStatus siteStatus = new CoffeeSiteStatus();
        siteStatus.setStatus(siteStatusEn.getSiteStatus());
        coffeeSite.setStatusZarizeni(siteStatus);
        return this;
    }
    
    public CoffeeSiteBuilder setSiteType(String siteTypeStr) {
        CoffeeSiteType siteType = new CoffeeSiteType();
        siteType.setCoffeeSiteType(siteTypeStr);
        coffeeSite.setTypPodniku(siteType);
        return this;
    }
    
    public CoffeeSiteBuilder setRecordStatus(CoffeeSiteRecordStatusEnum recordStatusE) {
        CoffeeSiteRecordStatus recordStatus = new CoffeeSiteRecordStatus();
        recordStatus.setStatus(recordStatusE.getSiteRecordStatus());
        coffeeSite.setRecordStatus(recordStatus);
        return this;
    }
    
    public CoffeeSiteBuilder setPriceRange(String priceRange) {
        PriceRange pr = new PriceRange();
        pr.setPriceRange(priceRange);
        coffeeSite.setCena(pr);
        return this;
    }
    
    public CoffeeSiteBuilder setPriceRange(PriceRange priceRange) {
        coffeeSite.setCena(priceRange);
        return this;
    }
    
    public CoffeeSiteBuilder setCoffeeSort(String ... coffeeSorts) {
        Set<CoffeeSort> cfSorts = new HashSet<>();
        
        for (String cs : coffeeSorts) {
            CoffeeSort cfSort = new CoffeeSort();
            cfSort.setCoffeeSort(cs);
            cfSorts.add(cfSort);
        }
        coffeeSite.setCoffeeSorts(cfSorts);
        return this;
    }
    
    public CoffeeSiteBuilder setCoffeeSort(Set<CoffeeSort> coffeeSorts) {
        coffeeSite.setCoffeeSorts(coffeeSorts);
        return this;
    }
    
    public CoffeeSiteBuilder setCups(Set<CupType> cups) {
        coffeeSite.setCupTypes(cups);
        return this;
    }
    
    public CoffeeSiteBuilder setCups(CupTypeEnum ... cupsEnum) {
        Set<CupType> cups = new HashSet<>();
        
        for (CupTypeEnum cup : cupsEnum) {
            CupType cupT = new CupType();
            cupT.setCupType(cup.getCupType());                  
            cups.add(cupT);
        }
        
        coffeeSite.setCupTypes(cups);
        return this;
    }
    
    public CoffeeSiteBuilder setCompany(Company company) {
        coffeeSite.setDodavatelPodnik(company);
        return this;
    }
    
    public CoffeeSiteBuilder setCompany(String company) {
        Company comp = new Company();
        comp.setNameOfCompany(company);
        coffeeSite.setDodavatelPodnik(comp);
        return this;
    }
    
    public CoffeeSiteBuilder setNextToMachineTypes(Set<NextToMachineType> ntmtSet) {
        coffeeSite.setNextToMachineTypes(ntmtSet);
        return this;
    }
    
    public CoffeeSiteBuilder setNextToMachineTypes(NextToMachineTypeEnum ... ntmts) {
        Set<NextToMachineType> ntmtSet = new HashSet<>(); 
        for (NextToMachineTypeEnum mtE : ntmts) {
            NextToMachineType mt = new NextToMachineType();
            mt.setType(mtE.getNexToMachineType());
            ntmtSet.add(mt);           
        }
        coffeeSite.setNextToMachineTypes(ntmtSet);
        return this;
    }

    public CoffeeSiteBuilder setOffer(Set<OtherOffer> offer) {
        coffeeSite.setOtherOffers(offer);
        return this;
    }
    
    public CoffeeSiteBuilder setOffer(String ... offers) {
        Set<OtherOffer> nabidka = new HashSet<>();
        for (String of : offers) {
            OtherOffer offerItem = new OtherOffer();
            offerItem.setOffer(of);
            nabidka.add(offerItem);
        }
        coffeeSite.setOtherOffers(nabidka);
        return this;
    }
    
    public CoffeeSiteBuilder setLocationType(SiteLocationType locT) {
        coffeeSite.setTypLokality(locT);
        return this;
    }
    
    public CoffeeSiteBuilder setLocationType(String locT) {
        SiteLocationType locType = new SiteLocationType();
        locType.setLocationType(locT);
        coffeeSite.setTypLokality(locType);
        return this;
    }
    
    public CoffeeSiteBuilder setOriginalUser(User user) {
        coffeeSite.setOriginalUser(user);
        return this;
    }
    
    public CoffeeSite build() {
        return coffeeSite;
    }
}