/**
 * 
 */
package cz.fungisoft.coffeecompass.testutils;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Company;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;


/**
 * Pomocna trida pro ulozeni vsech objektovych atributu CoffeeSitu, ktere maji v DB samostatnou
 * tab.
 * <p>
 * Zatim nevyuzito, ale lze pouzit pri testech, ktere pracuji primo s Repository, bez mockovani.
 * <p>
 * Pokud by melo byt vyuzito, bude potreba vlozit anotaci @Component aby vlozil Spring? Pravdepodobne ne,
 * melo by se pouzit jen pro Unit testy a zde neni Spring potreba? 
 * 
 * @author Michal Vaclavek
 *
 */
public class CoffeeSiteAttributesDBSaver {

    private final TestEntityManager entityManager;
    
    private static PriceRange pr = new PriceRange();
    private static Set<CoffeeSort> csorts = new HashSet<>();
    private static Set<CupType> cups = new HashSet<>();
    private static Company comp = new Company();
    private static Set<NextToMachineType> ntmtSet = new HashSet<>(); 
    private static Set<OtherOffer> nabidka = new HashSet<>();
    private static SiteLocationType locType = new SiteLocationType();
    private static User origUser = new User();
    private static CoffeeSiteStatus siteStatus = new CoffeeSiteStatus();
    private static CoffeeSiteType siteType = new CoffeeSiteType();
    
    
    public CoffeeSiteAttributesDBSaver(TestEntityManager entityManager2) {
        this.entityManager = entityManager2;
    }
    
    
    /**
     * Saves all object atributes of CoffeeSite to it's DB tables. CoffeeSite itself is not saved.
     * 
     * @param csToSave
     */
    @Transactional
    public void saveCoffeeSiteAtributesToDB(CoffeeSite csToSave) {        
        pr = csToSave.getCena();
        entityManager.persist(pr);
        
        siteType = csToSave.getTypPodniku();
        entityManager.persist(siteType);
               
        csorts = csToSave.getCoffeeSorts();
        for (CoffeeSort cs : csorts) {
            entityManager.persist(cs);
        }
            
        cups = csToSave.getCupTypes();
        
        for (CupType cup : cups) {
            entityManager.persist(cup);            
        }
        
        comp = csToSave.getDodavatelPodnik();
        entityManager.persist(comp);              
        
        ntmtSet = csToSave.getNextToMachineTypes();
        for (NextToMachineType mt : ntmtSet) {
            entityManager.persist(mt);
        }
       
        nabidka = csToSave.getOtherOffers();

        for (OtherOffer offer : nabidka) {
            entityManager.persist(offer);
        }
       
        locType = csToSave.getTypLokality();
        entityManager.persist(locType);
                
        for (UserProfile userProf : csToSave.getOriginalUser().getUserProfiles()) {
            entityManager.persist(userProf);            
        }
        
        origUser = csToSave.getOriginalUser();
        entityManager.persist(origUser);
        
        siteStatus = csToSave.getStatusZarizeni();
        entityManager.persist(siteStatus);
        
        entityManager.persist(csToSave.getRecordStatus());

        entityManager.flush();     
    }
    
}
