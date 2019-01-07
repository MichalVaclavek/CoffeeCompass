package cz.fungisoft.coffeecompass.controller.rest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
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
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.controller.CoffeeSiteSearchCriteriaModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
import cz.fungisoft.coffeecompass.dto.UserDataDto;
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
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.CoffeeSort.CoffeeSortEnum;
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
 * Zatím jen pomocná, nevyužitá třída/kontroler, který bude použit v případě využití REST rozhraní
 *  Základní Controller pro obsluhu požadavků, které se týkají práce s hlavním objektem CoffeeSite.<br>
 * Tj. pro základní CRUD operace a pro vyhledávání CoffeeSites.<br>
 * Tato verze je urcena pro REST,
 * pro testovaci/prototypovaci ucely je urcena verze CoffeeSiteController, ktera vyuziva system/framework Thymeleaf
 * <br>
 * @author Michal Václavek
 *
 */

@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/site") // uvadi se, pokud vsechny dotazy v kontroleru maji zacinat timto retezcem
public class CoffeeSiteControllerREST
{
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
     * Dependency Ijection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.
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
     * http://localhost:8080/rest/site/allSites/?orderBy=siteName&direction=asc<br>
     * http://localhost:8080/rest/site/allSites/?orderBy=cena&direction=asc<br>
     * 
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping("/allSites") 
    public ResponseEntity<List<CoffeeSiteDto>> items(@RequestParam(defaultValue = "id") String orderBy,
                                     @RequestParam(defaultValue = "asc") String direction) {
        return new ResponseEntity<List<CoffeeSiteDto>>(coffeeSiteService.findAll(orderBy, direction), HttpStatus.OK);
    }
    

    @GetMapping("/{id}") // napr. http://localhost:8080/rest/site/2
    public ResponseEntity<CoffeeSiteDto> siteById(@PathVariable Long id) {
        return new ResponseEntity<CoffeeSiteDto>(coffeeSiteService.findOneToTransfer(id), HttpStatus.OK);
    }
    
    
    @PostMapping("/modify/{id}") // napr. http://localhost:8080//rest/site/modify/2
    public void modifySiteToModifyById(@PathVariable Long id, @ModelAttribute CoffeeSite coffeeSite) {
        coffeeSite.setId(id);
        coffeeSiteService.save(coffeeSite);
    }
    
    @GetMapping("/") // napr. http://localhost:8080/rest/site/?name=test1
    public ResponseEntity<CoffeeSiteDto> siteByName(@RequestParam(value="name") String name) {
        return new ResponseEntity<CoffeeSiteDto>(coffeeSiteService.findByName(name), HttpStatus.OK);
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
    @GetMapping("/getSitesInRange/") // napr. http://localhost:8080/rest/site/getSitesInRange/?lat1=50.1669497&lon1=14.7657927&range=50000
    public ResponseEntity<List<CoffeeSiteDto>> sitesWithinRange(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                                                @RequestParam(value="range") long rangeMeters) {
        return new ResponseEntity<List<CoffeeSiteDto>>(coffeeSiteService.findAllWithinCircle(lat1, lon1, rangeMeters), HttpStatus.OK);
    }
    
    
    /**
     *  REST/JSON varianta obsluhu POST pozadavku z klienta, ktery obsahuje vyplnene hodnoty vyhledavacich kriterii
     *  pro vyhledavani CoffeeSites.<br>
     *  
     * @param lat1
     * @param lon1
     * @param rangeMeters
     * @param status
     * @param sort
     * @return
     */
    @PostMapping("/searchSites") // napr. http://localhost:8080/rest/site/searchSites/?lat1=50.1669497&lon1=14.7657927&range=50000&status=Opened&sort=espresso
    public ResponseEntity<List<CoffeeSiteDto>> searchSitesWithStatusAndCoffeeSort(@RequestParam(value="lat1") double lat1,
                                                                  @RequestParam(value="lon1") double lon1,
                                                                  @RequestParam(value="range") long rangeMeters,
                                                                  @RequestParam(value="status", defaultValue="Opened") String status,
                                                                  @RequestParam(value="sort", defaultValue="espresso") String sort) {
        
        return new ResponseEntity<List<CoffeeSiteDto>>(coffeeSiteService.findAllWithinCircleWithCSStatusAndCoffeeSort(lat1, lon1, rangeMeters, sort, status), HttpStatus.OK); // REST/JSON
    }

   
    /**
     * Obsluha POST pozadavku ze stranky obsahujici formular pro vytvoreni CoffeeSite.
     * 
     * @Valid zajisti, ze se pred zavolanim metody zvaliduje Coffee Site podle limitu, ktere jsou u atributu coffeeSite
     *
     * @param coffeeSite
     * @return
     */
    @PostMapping("/createSite") // Mapovani http POST na DB save/INSERT
    public ResponseEntity<Void> insert(@Valid @RequestBody CoffeeSiteDto coffeeSite, UriComponentsBuilder ucBuilder) {
       coffeeSiteService.save(coffeeSite);
    
       HttpHeaders headers = new HttpHeaders();
       headers.setLocation(ucBuilder.path("/rest/site/{id}").buildAndExpand(coffeeSite.getId()).toUri());
       return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }
    

    @PutMapping("/{id}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite
    public ResponseEntity<CoffeeSiteDto> updateRest(@PathVariable Long id, @Valid @RequestBody CoffeeSiteDto coffeeSite) {
        coffeeSite.setId(id);
        coffeeSiteService.save(coffeeSite);
        
        return new ResponseEntity<CoffeeSiteDto>(coffeeSiteService.findOneToTransfer(id), HttpStatus.OK);
    }
    
    /**
     * Smazani CoffeeSite daneho id
     * 
     * @param id
     */
    @DeleteMapping("/{id}") // Mapovani http DELETE na DB operaci delete
    public ResponseEntity<CoffeeSiteDto> delete(@PathVariable Long id) {
        coffeeSiteService.delete(id);
        return new ResponseEntity<CoffeeSiteDto>(HttpStatus.NO_CONTENT);
    }
    
    
    /* *** Atributes for coffeesite_create.html and other Forms **** */
    
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
