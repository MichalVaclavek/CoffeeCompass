package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.CupType;
import cz.fungisoft.coffeecompass.entity.Image;
import cz.fungisoft.coffeecompass.entity.NextToMachineType;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import cz.fungisoft.coffeecompass.entity.PriceRange;
import cz.fungisoft.coffeecompass.entity.SiteLocationType;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.User;
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
import cz.fungisoft.coffeecompass.service.comment.ICommentService;
import cz.fungisoft.coffeecompass.service.image.ImageStorageService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.service.weather.WeatherApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Základní Controller pro obsluhu požadavků, které se týkají práce s hlavním objektem CoffeeSite.<br>
 * Tj. pro základní CRUD operace a pro vyhledávání CoffeeSites.<br>
 * Tato verze je urcena pro pouziti se sablonovacim html/template systemem Thymeleaf, pro REST je/bude vytvorena
 * extra verze CoffeeSiteRESTController.
 * <br>
 * 
 * @author Michal Václavek
 */
@Controller
public class CoffeeSiteController {

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
    private NextToMachineTypeService ntmtService;
    
    @Autowired
    private CoffeeSiteTypeService coffeeSiteTypeService;
    
    @Autowired
    private CoffeeSortService coffeeSortService;
    
    @Autowired
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    @Autowired
    private ICommentService commentsService;
   
    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageStorageService imageStorageService;
    
    @Autowired
    private WeatherApiService weatherService;

    private final CoffeeSiteService coffeeSiteService;
    
    /**
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.<br>
     * 
     * Ale protoze techto servicu obsahuje tento Controler mnoho, bude konstruktor obsahovat pouze jeden
     * parametr se zakladnim Servicem CoffeeSiteService.<br>
     * Ostatni service budou @Autowired jako atributy instance, Spring zaridi injection. Spravnejsi je ale pres setter nebo prave konstruktor.
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteController(CoffeeSiteService coffeeSiteService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
    }

    /**
     * Zakladni obsluzna metoda pro zobrazeni seznamu CoffeeSite. Tato verze zobrazuje seznam CoffeeSitu po jednotlivych strankach.<br>
     * Priklady http dotazu, ktere vrati serazeny seznam CoffeeSitu jsou:
     *
     * http://localhost:8080/allSitesPaginated/?orderBy=siteName&direction=asc
     * http://localhost:8080/allSitesPaginated/?orderBy=siteName&direction=asc&page=1&size=5
     * https://localhost:8443/allSitesPaginated/?orderBy=createdOn&direction=desc&size=5&page=1
     * https://localhost:8443/allSitesPaginated/?size=5&page=1
     * 
     * apod.
     *
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping("/allSitesPaginated/")
    public ModelAndView sitesPaginated(@RequestParam(defaultValue = "createdOn") String orderBy,
                                       @RequestParam(defaultValue = "desc") String direction,
                                       @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        
        ModelAndView mav = new ModelAndView();
        
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(20);
        Page<CoffeeSiteDTO> coffeeSitePage;
        
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
 
        if (loggedInUser.isPresent() &&  userService.hasADMINorDBARole(loggedInUser.get())) {
            coffeeSitePage = coffeeSiteService.findAllPaginated(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString(direction.toUpperCase()), orderBy)));
        } else {
            coffeeSitePage = coffeeSiteService.findAllWithRecordStatusPaginated(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString(direction.toUpperCase()), orderBy)), CoffeeSiteRecordStatusEnum.ACTIVE);
        }
 
        mav.addObject("coffeeSitePage", coffeeSitePage);
 
        int totalPages = coffeeSitePage.getTotalPages();
        if (totalPages > 1) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                                                 .boxed()
                                                 .collect(Collectors.toList());
            mav.addObject("pageNumbers", pageNumbers);
        }
        
        mav.setViewName("coffeesites_info");
    
        return mav;       
    }

    /**
     * Metoda pro zobrazeni seznamu vsech CoffeeSites v mape.
     *
     * https://localhost:8443/allSitesInMap
     *
     * @return
     */
    @GetMapping("/allSitesInMap")
    public ModelAndView allSitesInMap() {
        ModelAndView mav = new ModelAndView();

        List<CoffeeSiteDTO> allCoffeeSites;

        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();

        if (loggedInUser.isPresent() && userService.hasADMINorDBARole(loggedInUser.get())) {
            allCoffeeSites = coffeeSiteService.findAll("createdOn", "DESC");
        } else {
            allCoffeeSites = coffeeSiteService.findAllWithRecordStatus(CoffeeSiteRecordStatusEnum.ACTIVE);
        }

        mav.addObject("allSites", allCoffeeSites);

        mav.setViewName("coffeesites_all_map");
        return mav;
    }
    
    /**
     * Vrati stranku zobrazujici vsechny informace pro jeden CoffeeSite. Pokud je prihlaseny nejaky uzivatel,<br>
     * zobrazi se moznost zadani hodnoceni (hvezdicky) a pridani komentare. Pokud uzivatel zobrazi<br>
     * site, ktery sam zalozil, zobrazuji se tlacitka pro zmenu stavu a pro Modifikaci.<br>
     * Model musi obsahovat take polozky pro zadani hodnoceni (stars) a komentare a polozku pro vlozeni Image, obrazku situ.
     * 
     * @param siteId of CoffeeSite to show
     * @return
     */
    @GetMapping("/showSite/{siteId}") // napr. http://localhost:8080/showSite/2
    public ModelAndView showSiteDetailForm(@PathVariable Long siteId, ModelMap model) {
        
        ModelAndView mav = new ModelAndView("404");

        return coffeeSiteService.findOneToTransfer(siteId).map(cs -> {
            // Add CoffeeSite to model
            mav.addObject("coffeeSite", cs);

            // Add object to cary users's comment and stars evaluation
            // If a user is logged-in, than find if he already saved comment for this site. If yes, this stars to model.
            StarsAndCommentModel starsAndComment = new StarsAndCommentModel();

            StarsQualityDescription userStarsForThisSite = starsForCoffeeSiteService.getStarsForCoffeeSiteAndLoggedInUser(cs);
            if (userStarsForThisSite != null )
                starsAndComment.setStars(userStarsForThisSite);

            mav.addObject("starsAndComment", starsAndComment);

            // Add all comments for this coffeeSite
            List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(siteId);
            mav.addObject("comments", comments);

            // Add current image ID of the CoffeeSite if available - to allow its deletition in a View
            Integer thisSiteImageId = imageStorageService.getImageIdForSiteId(siteId);
            thisSiteImageId = (thisSiteImageId == null) ? 0 : thisSiteImageId;

            mav.addObject("thisSiteImageId", thisSiteImageId); // needed in model in case of deletition request

            // Add new Image to model
            if (!model.containsAttribute("newImage")) { // othervise returned after validation error and model already contains newImage object
                Image newImage = new Image();
                newImage.setId(0);

                newImage.setCoffeeSiteID(siteId); // pomocny atribut. needed for later upload/save within ImageUploadController
                mav.addObject("newImage", newImage);
            }

            // Add picture object (image of this coffee site) to the model
            // Here inserted on the server, not as URL REST link to be loaded from browser
            String picString = imageStorageService.getImageAsBase64ForSiteId(cs.getId());
            mav.addObject("pic", picString);

            // Get Weather info for the geo location of the CofeeSite to be shown
            weatherService.getWeatherDTO(cs)
                          .ifPresent(w ->  mav.addObject("weatherData", w));


            mav.setViewName("coffeesite_detail");
            return mav;
        }).orElse(mav);
    }
    
    
    /**
     * Method to handle request to show CoffeeSites created by logged in user.
     * Result is Paginated and sorted by createdOn DESC.
     * 
     * @return
     */
    @GetMapping("/mySitesPaginated/") // napr. https://coffeecompass.cz/mySitesPaginated/?size=5&page=1
    public ModelAndView showMySitesPaginated(@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        
        ModelAndView mav = new ModelAndView();
        
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        Page<CoffeeSiteDTO> coffeeSitePage;
        
        coffeeSitePage = coffeeSiteService.findAllFromLoggedInUserPaginated(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString("DESC"), "createdOn")));
 
        if (coffeeSitePage != null) {
            mav.addObject("coffeeSitePage", coffeeSitePage);
        
            int totalPages = coffeeSitePage.getTotalPages();
            if (totalPages > 1) {
                List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                                                     .boxed()
                                                     .collect(Collectors.toList());
                mav.addObject("pageNumbers", pageNumbers);
            }
        }
        
        // to distinguish, if the page links on coffeesites_info.html should lead to allSitesPaginated controller method
        // or to this controller method i.e. to show links to other pages of coffeeSites of the logged-in user 
        mav.addObject("usersSitesList", true); 
        
        mav.setViewName("coffeesites_info");
    
        return mav;   
    }
    
    /**
     * Pomocna metoda pro otestovani, ze funguje volani Stored procedure v DB.
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    @GetMapping("/dist/") // napr. http://coffeecompass.cz/dist/?lat1=50.235&lon1=14.235&lat2=50.335&lon2=14.335
    public double distance(@RequestParam(value="lat1") double lat1, @RequestParam(value="lon1") double lon1,
                           @RequestParam(value="lat2") double lat2, @RequestParam(value="lon2") double lon2) {
        return coffeeSiteService.getDistance(lat1, lon1, lat2, lon2);
    }
    

   /**
    * Obsluha pozadavku na zobrazeni stranky/formulare pro vytvoreni noveho CoffeeSite<br>
    * tj. po clicku na polozku v header.html menu New Site
    * <br> 
    * Zobrazi stranku s formularem k vytvoreni noveho CoffeeSite.
    * 
    * @param coffeeSite - nove vytvorena instance CoffeeSite pro vlozeni do ModelView pro zobrazeni ve formulari/strance
    * pro vytvoreni/modifikaci.
    * @return {@code ModelAndView} pro zobrazeni formulare s hodnotami noveho/stavajiciho CoffeeSite.
    */
    @GetMapping(value = {"/createSite"})
    public ModelAndView siteToCreateAndModify(final CoffeeSiteDTO coffeeSite) {
        
        CoffeeSiteDTO cs;
        
        cs = coffeeSite; // novou instanci CoffeeSite vytvori Spring ? v konstruktoru
        cs.setId(0L); // je potreba zadat ID=0, aby se v Thymeleaf formulari dalo rozlisit, ze jde o novy CoffeeSite a zobrazit spravny Label text aj.
        cs.setCreatedOn(LocalDateTime.now());

        ModelAndView mav = new ModelAndView();
        
        mav.addObject("coffeeSite", cs);
        mav.setViewName("coffeesite_create");
        return mav;
    }
    
    /**
     * Obsluha pozadavku GET se stranky se seznamem CoffeeSite (coffeesites_info), po stisku tlacitka Modify, tj.<br>
     * pozadavek na Modifikaci daneho CoffeeSite.
     * <br>
     * Ukaze formular pro modifikaci/vytvoreni CoffeeSite "coffeesite_create.html" se spravne vyplnenymi polozkami<br>
     * daneho CoffeeSite id.
     * 
     * @param id
     * @return
     */
    @GetMapping("/modifySite/{id}")
    public ModelAndView showSiteUpdatePage(@PathVariable(name = "id") Long id) {
        ModelAndView mav = new ModelAndView("404");
        return coffeeSiteService.findOneToTransfer(id).map(cs -> {
            mav.addObject("coffeeSite", cs);
            mav.setViewName("coffeesite_create");
            return mav;
        }).orElse(mav);
    }
   
    /**
     * Zpracuje POST request/formular ze stranky pro vytvareni/modifikaci CoffeeSite objektu.<br>
     * Po uspesnem zpracovani pozadavku na vytvoreni noveho CoffeeSite se zobrazi stranka coffeesite_detail,<br>
     * ktera umozni aktivovat novy site nebo ho modifikovat.<br>
     * Po uspesnem zpracovani pozadavku na modifikaci CoffeeSite se zobrazi stejna stranka tj. umoznuje<br>
     * provest dalsi modifikace na prave editovanem CoffeeSitu.
     * <p>
     * Overeni, zda zadana pozice CoffeeSitu neni jiz pouzita neni potreba, protoze se overuje az pri<br>
     * pokusu o aktivaci, resp. vlozi se jako atribut do CoffeeSiteDTO objektu pri jeho vytvarni pred<br>
     * jeho odeslanim na server resp. pred zpracovanim Thymeleafem.
     * 
     * @param coffeeSite - novy/modifikovany objekt CoffeeSite z modelu, ktery vrati Spring/Thymeleaf, ktery ho vytvoril z formulare
     *                     pro vytvoreni/modifikaci CoffeeSite
     * @return
     */
    @PostMapping("/createModifySite") // Mapovani http POST na DB SAVE/UPDATE
    public String createOrUpdateCoffeeSite(@ModelAttribute("coffeeSite") @Valid CoffeeSiteDTO coffeeSite, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "coffeesite_create";
        }
        
        String returnView = "redirect:/showSite/";        
        String returnSuffix = "?createSuccess";
        
        CoffeeSite cs = null;
        
        if (coffeeSite.getId() != 0) { // modify CoffeeSite
            returnSuffix = "?modifySuccess";
            cs = coffeeSiteService.updateSite(coffeeSite);
        } else {
            cs = coffeeSiteService.save(coffeeSite);
        }
        
        returnView = (cs != null) ? returnView + cs.getId() + returnSuffix
                                  : "404";
        return returnView;
    }
    
    /**
     *  Zpracovani pozadavku na zmenu stavu CoffeeSite do stavu ACTIVE.<br>
     *  Pokud aktivaci provedl ADMIN, zobrazi se mu nasledni seznam vsech CoffeeSites,<br>
     *  jinak se zobrazi seznam všech CoffeeSites, které vytvořil daný user.<br>
     */
    @PutMapping("/activateSite/{id}") 
    public String activateCoffeeSite(@PathVariable(name = "id") Long id) {
        
        AtomicReference<String> returnPage = new AtomicReference<>("404");
        coffeeSiteService.findOneById(id).ifPresent(cs -> {
            if (coffeeSiteService.isLocationAlreadyOccupiedByActiveSite(cs.getZemSirka(), cs.getZemDelka(), 5, cs.getId())) {
                returnPage.set("redirect:/showSite/" + cs.getId() + "?anyOtherSiteActiveOnSamePosition");
            } else {
                cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, CoffeeSiteRecordStatusEnum.ACTIVE);
                // After CoffeeSite activation, go to the same page and show confirmation message
                returnPage.set("redirect:/showSite/" + cs.getId() + "?activationSuccess");
            }
        });
        return returnPage.get();
    }
    
    @PutMapping("/deactivateSite/{id}") 
    public String deactivateCoffeeSite(@PathVariable(name = "id") Long id) {
        return modifyStatusAndReturnSameView(id, CoffeeSiteRecordStatusEnum.INACTIVE);
    }

    @PutMapping("/cancelStatusSite/{id}") 
    public String cancelStatusSite(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        // ADMIN or DBA can still continue in CoffeeSite view page to modify site's status
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser.isPresent() &&  userService.hasADMINorDBARole(loggedInUser.get())) {
            return modifyStatusAndReturnSameView(id, CoffeeSiteRecordStatusEnum.CANCELED);
        }
        else { // Normal USER is redirected to list of his/her sites after cancelling site
            return coffeeSiteService.findOneById(id).map(cs -> {
                cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, CoffeeSiteRecordStatusEnum.CANCELED);
                String siteName = cs.getSiteName();
                redirectAttributes.addFlashAttribute("canceledSiteName", siteName);
                return  "redirect:/mySitesPaginated/";
            }).orElse("404");
        }
    }

    /**
     * Pomocna metoda zdruzujici Controller prikazy pro některé modifikace stavu CoffeeSitu.
     * Pouzito ale jen pro CoffeeSiteRecordStatusEnum.INACTIVE a CoffeeSiteRecordStatusEnum.CANCELED
     * ostatni maji specialni redirect
     */
    private String modifyStatusAndReturnSameView(Long csID, CoffeeSiteRecordStatusEnum newStatus) {
        return coffeeSiteService.findOneById(csID).map(cs -> {
            cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, newStatus);
            return "redirect:/showSite/" + cs.getId();
        }).orElse("404");
    }
    
    /**
     * Zpracovani pozadavku http/DELETE z formulare s DELETE requestem
     * pro id daneho CoffeeSite - zavola service metodu pro smazani CoffeeSite z DB s danym id
     * a zobrazi aktualizovany seznam CoffeeSites. Muze volat pouze user s pravy ADMIN.
     *  
     * @param id - CoffeeSitu, ktery se ma definitivne odstranit z DB.
     * @return
     */
    @DeleteMapping("/finalDeleteSite/{id}") // Mapovani http DELETE na DB operaci delete
    public String finalDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        AtomicReference<String> returnPage = new AtomicReference<>("404");
        coffeeSiteService.findOneById(id).ifPresent(cs -> {
            String siteName = cs.getSiteName();
            redirectAttributes.addFlashAttribute("deletedSiteName", siteName);
            coffeeSiteService.delete(id);
            returnPage.set("redirect:/allSitesPaginated/");
        });

        return returnPage.get();
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
        
    @ModelAttribute("allNextToMachineTypes")
    public List<NextToMachineType> populateNextToMachineTypes() {
        return ntmtService.getAllNextToMachineTypes();
    }
    
    @ModelAttribute("allCoffeeSiteTypes")
    public List<CoffeeSiteType> populateCoffeeSiteTypes() {
        return coffeeSiteTypeService.getAllCoffeeSiteTypes();
    }
    
    @ModelAttribute("allCoffeeSorts")
    public List<CoffeeSort> populateCoffeeSorts() {
        return coffeeSortService.getAllCoffeeSorts();
    }
}