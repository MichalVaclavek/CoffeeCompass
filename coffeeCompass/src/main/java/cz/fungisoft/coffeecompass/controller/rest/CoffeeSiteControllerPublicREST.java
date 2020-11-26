package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.service.CSRecordStatusService;
import cz.fungisoft.coffeecompass.service.CSStatusService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteTypeService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import cz.fungisoft.coffeecompass.service.CupTypeService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
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
 * <br>
 * @author Michal Václavek
 *
 */
@Api // Swagger
@RestController 
@RequestMapping("/rest/site") 
public class CoffeeSiteControllerPublicREST
{
    private static final Logger log = LoggerFactory.getLogger(CoffeeSiteControllerPublicREST.class);
    
    @Autowired
    private OtherOfferService offerService;
    
    @Autowired
    private CSStatusService csStatusService;
    
    @Autowired
    private CSRecordStatusService csRecordStatusService;
    
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
    
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
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
    public CoffeeSiteControllerPublicREST(CoffeeSiteService coffeeSiteService,
                                          IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
        this.starsForCoffeeSiteService = starsForCoffeeSiteService;
    }

    /**
     * Priklady http dotazu, ktere vrati serazeny seznam CoffeeSitu jsou:
     * <br>
     * http://coffeecompass.cz/rest/site/allSites/?orderBy=siteName&direction=asc<br>
     * http://coffeecompass.cz/rest/site/allSites/?orderBy=cena&direction=asc<br>
     * http://coffeecompass.cz/rest/site/allSites/
     * 
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping("/allSites/") 
    public ResponseEntity<List<CoffeeSiteDTO>> sites(@RequestParam(defaultValue = "id") String orderBy,
                                                     @RequestParam(defaultValue = "asc") String direction) {
        
        List<CoffeeSiteDTO> coffeeSites = coffeeSiteService.findAll(orderBy, direction);
        
        if (coffeeSites == null || coffeeSites.isEmpty()) {
            log.error("No Coffee site found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } 
        
        log.info("All sites retrieved.");
        return new ResponseEntity<>(coffeeSites, HttpStatus.OK);
    }
    
    /**
     * Vrati 1 stranku ze vsech AKTIVNICH CoffeeSites.
     * 
     * Priklady http dotazu, ktere vrati serazeny seznam/stranku CoffeeSitu jsou:
     * <br>
     * https://coffeecompass.cz/rest/site/allSitesPaginated/<br>
     * https://coffeecompass.cz/rest/site/allSitesPaginated/?size=5&page=1<br>
     * https://coffeecompass.cz/rest/site/allSitesPaginated/?size=5&page=2&orderBy=createdOn&direction=asc<br>
     * 
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping("/allSitesPaginated/")
    @ResponseStatus(HttpStatus.OK)
    public Page<CoffeeSiteDTO> allSitesPaginated(@RequestParam(defaultValue = "createdOn") String orderBy,
                                                                 @RequestParam(defaultValue = "desc") String direction,
                                                                 @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<CoffeeSiteDTO> coffeeSitePage;
        
        // Get 1 page of all ACTIVE CoffeeSites
        coffeeSitePage = coffeeSiteService.findAllWithRecordStatusPaginated(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString(direction.toUpperCase()), orderBy)), CoffeeSiteRecordStatusEnum.ACTIVE);
        
        log.info("Page n. {} of coffee sites from logged-in user retrieved.", currentPage);
        return coffeeSitePage;
    }
    
    /**
     * Serves request to return CoffeeSite of the given id
     * 
     * @param id - id of the CoffeeSite to be returned
     * @return found CoffeeSiteDTO with requested id
     */
    @GetMapping("/{id}") // napr. http://coffeecompass.cz/rest/site/2
    public ResponseEntity<CoffeeSiteDTO> siteById(@PathVariable Long id) {
        
        CoffeeSiteDTO cs = coffeeSiteService.findOneToTransfer(id);
        
        return (cs == null) ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                            : new ResponseEntity<>(cs, HttpStatus.OK);
    }
    

    /**
     * Serves request to return CoffeeSite of the given name
     * 
     * @param name - name of CoffeeSite to return
     * @return found CoffeeSiteDTO
     */
    @GetMapping("/") // napr. http://coffeecompass.cz/rest/site/?name=test1
    public ResponseEntity<CoffeeSiteDTO> siteByName(@RequestParam(value="name") String name) {
        
        CoffeeSiteDTO cs = coffeeSiteService.findByName(name);
        
        return (cs == null) ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                            : new ResponseEntity<>(cs, HttpStatus.OK);
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
    @ResponseStatus(HttpStatus.OK)
    public Double distance(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
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
        
        if (result == null || result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else
            return new ResponseEntity<>(result, HttpStatus.OK);
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
        
        if (result == null || result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); 
        } else
            return new ResponseEntity<>(result, HttpStatus.OK); 
    }
    
    /**
     * REST endpoint for obtaining number of stars gived by userID to coffeeSiteID
     * If there was no rating for this site and user yet, then returns zero 0.
     * URL example: https://localhost:8443/rest/site/stars/?siteID=2&userID=1
     * 
     * @param siteID
     * @param userID
     * @return
     */
    @GetMapping("/stars/number/")
    @ResponseStatus(HttpStatus.OK)
    public Integer getNumberOfStarsForSiteFromUser(@RequestParam(value="siteID") Long siteID, @RequestParam(value="userID") Long userID) {
        Integer numOfStars = starsForCoffeeSiteService.getStarsForCoffeeSiteAndUser(siteID, userID);
        
        if (numOfStars == null) {
            numOfStars = 0;
        }
        
        return numOfStars;
    }
    
    /* *** Atributes needed for client creating/editing Coffee site **** */
    
    @GetMapping("/allOtherOffers")
    public List<OtherOffer> populateOtherOffers() {
        return offerService.getAllOtherOffers();
    }
       
    @GetMapping("/allSiteStatuses")
    public List<CoffeeSiteStatus> populateSiteStatuses() {
        return csStatusService.getAllCoffeeSiteStatuses();
    }
    
    @GetMapping("/allSiteRecordStatuses")
    public List<CoffeeSiteRecordStatus> populateSiteRecordStatuses() {
        return csRecordStatusService.getAllCSRecordStatuses();
    }
    
    @GetMapping("/allHodnoceniKavyStars")
    public List<StarsQualityDescription> populateQualityStars() {
        return starsQualityService.getAllStarsQualityDescriptions();
    }
    
    @GetMapping("/allPriceRanges")
    public List<PriceRange> populatePriceRanges() {
        return priceRangeService.getAllPriceRanges();
    }
    
    @GetMapping("/allLocationTypes")
    public List<SiteLocationType> populateLocationTypes() {
        return locationTypesService.getAllSiteLocationTypes();
    }
    
    @GetMapping("/allCupTypes")
    public List<CupType> populateCupTypes() {
        return cupTypesService.getAllCupTypes();
    }
        
    @GetMapping("/allCoffeeSorts")
    public List<CoffeeSort> populateCoffeeSorts() {
        return coffeeSortService.getAllCoffeeSorts();
    }
       
    @GetMapping("/allNextToMachineTypes")
    public List<NextToMachineType> populateNextToMachineTypes() {
        return ntmtService.getAllNextToMachineTypes();
    }
    
    @GetMapping("/allCoffeeSiteTypes")
    public List<CoffeeSiteType> populateCoffeeSiteTypes() {
        return coffeeSiteTypeService.getAllCoffeeSiteTypes();
    }
}
