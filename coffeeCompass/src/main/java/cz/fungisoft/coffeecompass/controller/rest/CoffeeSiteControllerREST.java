package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.controller.models.CoffeeSiteSearchCriteriaModel;
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
 * Zatím jen pomocná, nevyužitá třída/kontroler, který bude použit v případě využití REST rozhraní<br>
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
public class CoffeeSiteControllerREST
{
    private static final Logger log = LoggerFactory.getLogger(CoffeeSiteControllerREST.class);
    
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
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.
     * <br><br>
     * Ale protoze techto servicu bude v tomto Controleru mnoho, bude konstruktor obsahovat pouze jeden
     * parametr se zakladnim Servicem CoffeeSiteService.<br>
     * Ostatni service budou @Autowired jako atributy instance, Spring zaridi injection
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteControllerREST(CoffeeSiteService coffeeSiteService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
    }

    /**
     * Priklady http dotazu, ktere vrati serazeny seznam CoffeeSitu jsou:
     * <br>
     * http://coffeecompass.cz/rest/site/allSites/?orderBy=siteName&direction=asc<br>
     * http://coffeecompass.cz/rest/site/allSites/?orderBy=cena&direction=asc<br>
     * 
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping("/allSites") 
    public ResponseEntity<List<CoffeeSiteDTO>> sites(@RequestParam(defaultValue = "id") String orderBy,
                                                     @RequestParam(defaultValue = "asc") String direction) {
        
        List<CoffeeSiteDTO> coffeeSites = coffeeSiteService.findAll(orderBy, direction);
        log.info("All sites retrieved.");
        if (coffeeSites == null || coffeeSites.size() == 0) {
            log.error("No Coffee site found.");
            return new ResponseEntity<List<CoffeeSiteDTO>>(HttpStatus.NOT_FOUND);
        } 
        
        log.info("All sites retrieved.");
        return new ResponseEntity<List<CoffeeSiteDTO>>(coffeeSites, HttpStatus.OK);
    }
    

    @GetMapping("/{id}") // napr. http://coffeecompass.cz/rest/site/2
    public ResponseEntity<CoffeeSiteDTO> siteById(@PathVariable Long id) {
        
        CoffeeSiteDTO cs = coffeeSiteService.findOneToTransfer(id);
        
        if (cs == null) {
            return new ResponseEntity<CoffeeSiteDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<CoffeeSiteDTO>(cs, HttpStatus.OK);
    }
    
    
    @GetMapping("/") // napr. http://coffeecompass.cz/rest/site/?name=test1
    public ResponseEntity<CoffeeSiteDTO> siteByName(@RequestParam(value="name") String name) {
        
        CoffeeSiteDTO cs = coffeeSiteService.findByName(name);
        
        if (cs == null) {
            return new ResponseEntity<CoffeeSiteDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<CoffeeSiteDTO>(cs, HttpStatus.OK);
    }
    
    
    /**
     * Pomocna metoda pro otestovani, ze funguje volani Stored procedure v DB
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    @GetMapping("/dist/") // napr. http://localhost:8080/rest/site/dist/?lat1=50.235&lon1=14.235&lat2=50.335&lon2=14.335
    public double distance(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                           @RequestParam(value="lat2") double lat2, @RequestParam(value="lon2") double lon2) {
        return coffeeSiteService.getDistance(lat1, lon1, lat2, lon2);
    }
    
    /**
     * Obsluha dotazu pro vyhledani CoffeeSites objektu, ktere splnuji podminku definovanou @RequestParam parametry
     * v tomto pripade pro hledani podle polohy:<br>
     * 
     * @param lat1
     * @param lon1
     * @param rangeMeters
     * @return
     */
    @GetMapping("/getSitesInRange/") 
    public ResponseEntity<List<CoffeeSiteDTO>> sitesWithinRange(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                                                                @RequestParam(value="range") long rangeMeters) {
        
        List<CoffeeSiteDTO> result = coffeeSiteService.findAllWithinCircle(lat1, lon1, rangeMeters);
        
        if (result == null || result.size() == 0) {
            return new ResponseEntity<List<CoffeeSiteDTO>>(HttpStatus.NOT_FOUND);
        } else
            return new ResponseEntity<List<CoffeeSiteDTO>>(result, HttpStatus.OK);
    }
    
    
    /**
     *  REST/JSON varianta obsluhu POST pozadavku z klienta, ktery obsahuje vyplnene hodnoty vyhledavacich kriterii
     *  pro vyhledavani CoffeeSites.<br>
     *  napr. http://localhost:8080/rest/site/searchSites/?lat1=50.1669497&lon1=14.7657927&range=50000&status=Opened&sort=espresso<br>
     *  nebo<br>
     *  http://localhost:8080/rest/site/searchSites/?lat1=50.1669497&lon1=14.7657927&range=50000&status=V%20provozu&sort=?<br>
     *  http://localhost:8080/rest/site/searchSites/?lat1=50.1669497&lon1=14.7657927&range=50000&sort=?
     *  pokud se nema filtovat podle druhu kavy.
     *  
     * @param lat1
     * @param lon1
     * @param rangeMeters
     * @param status
     * @param sort
     * @return
     */
    @GetMapping("/searchSites") 
    public ResponseEntity<List<CoffeeSiteDTO>> searchSitesWithStatusAndCoffeeSort(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                                                                                  @RequestParam(value="range") long rangeMeters,
                                                                                  @RequestParam(value="status", defaultValue="V provozu") String status,
                                                                                  @RequestParam(value="sort", defaultValue="espresso") String sort) {
        
        // CoffeeSort is not intended as a filter criteria id sort=?
        if ("?".equals(sort)) {
            sort = "";
        }
        
        List<CoffeeSiteDTO> result = coffeeSiteService.findAllWithinCircleWithCSStatusAndCoffeeSort(lat1, lon1, rangeMeters, sort, status);
        
        if (result == null || result.size() == 0) {
            return new ResponseEntity<List<CoffeeSiteDTO>>(HttpStatus.NOT_FOUND); 
        } else
            return new ResponseEntity<List<CoffeeSiteDTO>>(result, HttpStatus.OK); 
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
