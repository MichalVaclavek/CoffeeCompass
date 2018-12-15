package cz.fungisoft.coffeecompass.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
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
import cz.fungisoft.coffeecompass.entity.CoffeeSort.CoffeeSortEnum;
import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.pojo.Message;
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
 * @author Michal
 *
 */
/*
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/site") // uvadi se, pokud vsechny dotazy v kontroleru maji zacinat timto retezcem
*/
public class CoffeeSiteRESTController
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
     * 
     * Ale protoze techto servicu bude v tomto Controleru mnoho, bude konstruktor obsahovat pouze jeden
     * parametr se zakladnim Servicem CoffeeSiteService.
     * Ostatni service budou @Autowired jako atributy instance, Spring zaridi injection
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteRESTController(CoffeeSiteService coffeeSiteService)
    {
        super();
        this.coffeeSiteService = coffeeSiteService;
    }

    /**
     * Priklady http dotazu, ktere vrati serazeny seznam CoffeeSitu jsou:
     *
     * http://localhost:8080/sites/?orderBy=siteName&direction=asc
     * http://localhost:8080/sites/?orderBy=cena&direction=asc
     * 
     * apod.
     *
     * @param orderBy
     * @param direction
     * @return
     */
    /* REST - puvodni verze bez ModelAndView */
    @GetMapping("/allSites")
    public List<CoffeeSiteDto> items(@RequestParam(defaultValue = "id") String orderBy,
                                     @RequestParam(defaultValue = "asc") String direction)
    {
        return coffeeSiteService.findAll(orderBy, direction);
    }
    

    @GetMapping("/{id}") // napr. http://localhost:8080/site/2
    public CoffeeSiteDto siteById(@PathVariable int id)
    {
        return coffeeSiteService.findOneToTransfer(id);
    }
    
    
    @PostMapping("/modify/{id}") // napr. http://localhost:8080/modifySite/2
    public void modifySiteToModifyById(@PathVariable int id, @ModelAttribute CoffeeSite coffeeSite, final BindingResult bindingResult, final ModelMap model)
    {
        coffeeSite.setId(id);
        coffeeSiteService.save(coffeeSite);
    }
    
    @GetMapping("/") // napr. http://localhost:8080/site/?name=test1
    public CoffeeSiteDto siteByName(@RequestParam(value="name") String name)
    {
        return coffeeSiteService.findByName(name);
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
    @GetMapping("/dist/") // napr. http://localhost:8080/dist/?lat1=50.235&lon1=14.235&lat2=50.335&lon2=14.335
    public double distance(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                           @RequestParam(value="lat2") double lat2, @RequestParam(value="lon2") double lon2)
    {
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
    @GetMapping("/getSitesInRange/") // napr. http://localhost:8080/getSites/?lat1=50.1669497&lon1=14.7657927&range=50000
    public List<CoffeeSiteDto> sitesWithinRange(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                                                @RequestParam(value="range") long rangeMeters)
    {
        return coffeeSiteService.findAllWithinCircle(lat1, lon1, rangeMeters);
    }
    
    /**
     * Priprava dat pro clienta, ktery zobrazi stranku pro vyhledavani CoffeeSites podle {@link CoffeeSiteSearchCriteriaModel}
     * 
     * @param model
     * @return
     */
    @GetMapping("/searchSites") 
    public String getSitesWithStatusAndCoffeeSort(Model model)
    {
        CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
        
        // Defaultni hodnoty Search criterii. Defaultni pro polohu bude aktualni poloha zarizeni
        searchCriteria.setSortSelected(false);
        searchCriteria.setRange(500L);
        // Defaultni hodnoty pro rozbalovaci menu coffeeSort v pripade, ze uzivatel bude vybirat i podle coffeeSort
        searchCriteria.setCoffeeSort(CoffeeSortEnum.ESPRESSO.getCoffeeType());
        
        model.addAttribute("searchCriteria", searchCriteria);
        return "coffeesite_search";
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
    @PostMapping("/searchSites") // napr. http://localhost:8080/getSites/?lat1=50.1669497&lon1=14.7657927&range=50000&status=Opened&sort=espresso
    public List<CoffeeSiteDto> searchSitesWithStatusAndCoffeeSort(@RequestParam(value="lat1") double lat1,
                                                                  @RequestParam(value="lon1") double lon1,
                                                                  @RequestParam(value="range") long rangeMeters,
                                                                  @RequestParam(value="status", defaultValue="Opened") String status,
                                                                  @RequestParam(value="sort", defaultValue="espresso") String sort)
    {
        
        return coffeeSiteService.findAllWithinCircleWithCSStatusAndCoffeeSort(lat1, lon1, rangeMeters, sort, status); // REST/JSON
    }


   /*
    * Show form to create new CoffeeSite or to modify CoffeeSite.
    * Priprava a poslani dat pro zobrazeni stranky/formulare pro vytvoreni noveho nebo editaci stavajiciho CoffeeSite  
    */
    @GetMapping(value={"/createModifySite", "/createModifySite/{id}"})
    public String siteToCreateAndModify(final CoffeeSite coffeeSite,  @PathVariable(required = false, name = "id") Integer id)
    {
        CoffeeSite cs;
        
        if ((id == null) || (id == 0))
        {
            cs = coffeeSite;
            cs.setCreatedOn(new Timestamp(new Date().getTime()));
        }
        else
        {
            cs = coffeeSiteService.findOneById(id);
        }
        
        return "coffeesite_create";
    }
    
    /**
     * Obsluha POST pozadavku ze stranky obsahujici formular pro vytvoreni nebo editaci CoffeeSite.
     * 
     * @Valid zajisti, ze se pred zavolanim metody zvaliduje Coffee Site podle limitu, ktere jsou u atributu coffeeSite
     *
     * @param coffeeSite
     * @return
     */
    @PostMapping("/createModifySite") // Mapovani http POST na DB save/INSERT/UPDATE
    public String insert(@Valid @RequestBody CoffeeSite coffeeSite, final ModelMap model)
    {
       coffeeSiteService.save(coffeeSite);
       model.clear();
    
       // Zobrazeni stranky s prehledem vsech CoffeeSites nebo pouze se stavem/hodnotami jednoho vytvareneho/editovaneho CoffeeSite
       return "coffeesites_info";
    }
    

    @PutMapping("/{id}") // Mapovani http PUT na DB operaci UPDATE tj. zmena zaznamu c. id polozkou coffeeSite
    public void updateRest(@PathVariable int id, @Valid @RequestBody CoffeeSite coffeeSite)
    {
        coffeeSite.setId(id);
        coffeeSiteService.save(coffeeSite);
    }
    
    /**
     * Smazani CoffeeSite daneho id
     * 
     * @param id
     */
    @DeleteMapping("/{id}") // Mapovani http DELETE na DB operaci delete
    public void delete(@PathVariable int id)
    {
        coffeeSiteService.delete(id);
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
    
    /**
     * Tato metoda se vola pri vyjimkach vyhozenych metodami s anotacemi, ktere obsluhuje Spring.
     * 
     * @param e
     * @return
     */
    @ExceptionHandler  
    public ResponseEntity<Message> handleEntNotFound(EntityNotFoundException e)
    {
        // Co se ma poslat v html body v pripade vyjimky. HttpStatus.NOT_FOUND generuje response/status kod 404
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(e.getMessage()));
    }
    
    @ExceptionHandler  
    public ResponseEntity<Message> handleConstraintViolation(ConstraintViolationException e)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(e.getMessage()));
    }

}
