package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.service.CSStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteTypeService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import cz.fungisoft.coffeecompass.service.CupTypeService;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;
import cz.fungisoft.coffeecompass.service.OtherOfferService;
import cz.fungisoft.coffeecompass.service.PriceRangeService;
import cz.fungisoft.coffeecompass.service.SiteLocationTypeService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import io.swagger.annotations.Api;

/**
 * Třída/kontroler, který lze použit v případě využití REST rozhraní<br>
 * 
 * Základní Controller pro obsluhu požadavků, které se týkají práce s hlavním objektem CoffeeSite.<br>
 * Tj. pro základní CRUD operace a pro vyhledávání CoffeeSites.<br>
 * Tato verze je urcena pro REST, pro testovaci/prototypovaci ucely je urcena verze CoffeeSiteController, ktera vyuziva system/framework Thymeleaf
 * <br>
 * @author Michal Václavek
 *
 */
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/site") // vsechny http dotazy v kontroleru maji zacinat timto retezcem
//@RequiredArgsConstructor //TODO dodelat s pouzitim lombok, bez Autowired na fields
public class CoffeeSiteControllerSecuredREST
{
    private static final Logger log = LoggerFactory.getLogger(CoffeeSiteControllerSecuredREST.class);
    
    @Autowired
    private OtherOfferService offerService;
    
    @Autowired
    private CSStatusService csStatusService;
    
    @Autowired
    private StarsQualityService starsQualityService;
    
    @Autowired
    private PriceRangeService priceRangeService;
    
    @Autowired
    private SiteLocationTypeService locationTypesService;
    
    @Autowired
    private CupTypeService cupTypesService;
    
    @Autowired
    private CoffeeSortService coffeeSortService;
    
    @Autowired
    private NextToMachineTypeService ntmtService;
    
    @Autowired
    private CoffeeSiteTypeService coffeeSiteTypeService;
    
    private CoffeeSiteService coffeeSiteService;
    
    /**
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.<br>
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.
     * <p>
     * Ale protoze techto servicu bude v tomto Controleru mnoho, bude konstruktor obsahovat pouze jeden<br>
     * parametr se zakladnim Servicem CoffeeSiteService.<br>
     * Ostatni service budou @Autowired jako atributy instance, Spring zaridi injection
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteControllerSecuredREST(CoffeeSiteService coffeeSiteService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
    }

   
    /**
     * Obsluha POST pozadavku ze stranky obsahujici formular pro vytvoreni CoffeeSite.
     * 
     * @Valid zajisti, ze se pred zavolanim metody zvaliduje Coffee Site podle limitu, ktere jsou u atributu coffeeSite
     *
     * @param coffeeSite
     * @return
     */
    @PostMapping("/create") // Mapovani http POST na DB save/INSERT
    public ResponseEntity<Void> insert(@Valid @RequestBody CoffeeSiteDTO coffeeSite, UriComponentsBuilder ucBuilder) {
    
       CoffeeSite cs = coffeeSiteService.save(coffeeSite);
       
       HttpHeaders headers = new HttpHeaders();
       if (cs != null) {
           log.info("New Coffee site created.");
           headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(coffeeSite.getId()).toUri());
           return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
       }
       else {
           log.error("Coffee site creation failed");
           headers.setLocation(ucBuilder.path("/rest/site/create}").buildAndExpand(coffeeSite.getId()).toUri());
           return new ResponseEntity<Void>(headers, HttpStatus.BAD_REQUEST);
       }
    }
    

    @PutMapping("/update/{id}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite, napr. http://localhost:8080/rest/site/update/2
    public ResponseEntity<CoffeeSiteDTO> updateRest(@PathVariable Long id, @Valid @RequestBody CoffeeSiteDTO coffeeSite) {
        coffeeSite.setId(id);
        
        CoffeeSiteDTO cs = null;
        if (coffeeSiteService.save(coffeeSite) != null) {
            log.info("Coffee site update successful.");
            cs = coffeeSiteService.findOneToTransfer(id);
        } else
            log.error("Coffee site update failed.");
        
        return (cs != null) ? new ResponseEntity<CoffeeSiteDTO>(cs, HttpStatus.CREATED)
                            : new ResponseEntity<CoffeeSiteDTO>(HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Smazani CoffeeSite daneho id
     * 
     * @param id
     */
    @DeleteMapping("/delete/{id}") // Mapovani http DELETE na DB operaci delete, napr. http://localhost:8080/rest/site/delete/2
    public ResponseEntity<CoffeeSiteDTO> delete(@PathVariable Long id) {
        coffeeSiteService.delete(id);
        return new ResponseEntity<CoffeeSiteDTO>(HttpStatus.NO_CONTENT);
    }
    
    /* *** Atributes needed for client creating/editing Coffee site **** */
    
    @ModelAttribute("allOffers")
    public List<OtherOffer> populateOffers() {
        return offerService.getAllOtherOffers();
    }
       
    @ModelAttribute("allSiteStatuses")
    public List<CoffeeSiteStatus> populateSiteStatuses() {
        return csStatusService.getAllCoffeeSiteStatuses();
    }
    
    @ModelAttribute("allHodnoceniKavyStars")
    public List<StarsQualityDescription> populateQualityStars() {
        return starsQualityService.getAllStarsQualityDescriptions();
    }
    
    @ModelAttribute("allPriceRanges")
    public List<PriceRange> populatePriceRanges() {
        return priceRangeService.getAllPriceRanges();
    }
    
    @ModelAttribute("allLocationTypes")
    public List<SiteLocationType> populateLocationTypes() {
        return locationTypesService.getAllSiteLocationTypes();
    }
    
    @ModelAttribute("allCupTypes")
    public List<CupType> populateCupTypes() {
        return cupTypesService.getAllCupTypes();
    }
        
    @ModelAttribute("allCoffeeSorts")
    public List<CoffeeSort> populateCoffeeSorts() {
        return coffeeSortService.getAllCoffeeSorts();
    }
       
    @ModelAttribute("allNextToMachineTypes")
    public List<NextToMachineType> populateNextToMachineTypes() {
        return ntmtService.getAllNextToMachineTypes();
    }
    
    @ModelAttribute("allCoffeeSiteTypes")
    public List<CoffeeSiteType> populateCoffeeSiteTypes() {
        return coffeeSiteTypeService.getAllCoffeeSiteTypes();
    }

}
