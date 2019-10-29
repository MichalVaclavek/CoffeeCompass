package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
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
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.ImageStorageService;
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;
import cz.fungisoft.coffeecompass.service.OtherOfferService;
import cz.fungisoft.coffeecompass.service.PriceRangeService;
import cz.fungisoft.coffeecompass.service.SiteLocationTypeService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
public class CoffeeSiteController
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

    private CoffeeSiteService coffeeSiteService;
    
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
     * Zakladni obsluzna metoda pro zobrazeni seznamu CoffeeSite.<br>
     * Priklady http dotazu, ktere vrati serazeny seznam CoffeeSitu jsou:
     *
     * http://localhost:8080/allSites/?orderBy=siteName&direction=asc
     * http://localhost:8080/allSites/?orderBy=distFromSearchPoint&direction=asc
     * 
     * apod.
     *
     * @param orderBy
     * @param direction
     * @return
     */
    @GetMapping("/allSites")
    public ModelAndView sites(@RequestParam(defaultValue = "id") String orderBy, @RequestParam(defaultValue = "asc") String direction) {
        
        ModelAndView mav = new ModelAndView();
        
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser.isPresent() &&  userService.hasADMINorDBARole(loggedInUser.get()))
            mav.addObject("allSites", coffeeSiteService.findAll(orderBy, direction));  
        else
            mav.addObject("allSites", coffeeSiteService.findAllWithRecordStatus(CoffeeSiteRecordStatusEnum.ACTIVE));
        
        mav.setViewName("coffeesites_info");
    
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
        
        ModelAndView mav = new ModelAndView();
        
        // Add CoffeeSite to model
        CoffeeSiteDTO cs = coffeeSiteService.findOneToTransfer(siteId);
        
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
        
        mav.setViewName("coffeesite_detail");
        
        return mav;
    }
    
    /**
     * Method to handle request to show CoffeeSites created by logged in user.
     * 
     * @return
     */
    @GetMapping("/mySites") // napr. http://coffeecompass.cz/mySites
    public ModelAndView showMySites() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("allSites", coffeeSiteService.findAllFromLoggedInUser());
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
    @RequestMapping(value = {"/createSite"}, method = RequestMethod.GET)
    public ModelAndView siteToCreateAndModify(final CoffeeSiteDTO coffeeSite) {
        
        CoffeeSiteDTO cs;
        
        cs = coffeeSite; // novou instanci CoffeeSite vytvori Spring ? v konstruktoru
        cs.setId(0L); // je potreba zadat ID=0, aby se v Thymeleaf formulari dalo rozlisit, ze jde o novy CoffeeSite a zobrazit spravny Label text aj.
        cs.setCreatedOn(new Timestamp(new Date().getTime()));

        ModelAndView mav = new ModelAndView();
        
        mav.addObject("coffeeSite", cs);
        mav.setViewName("coffeesite_create");
        return mav;
    }
    
    /**
     * Obsluha pozadavku GET se stranky se seznamem CoffeeSite (coffeesites_info), po stisku tlacitka Modify, tj.
     * pozadavek na Modifikaci daneho CoffeeSite.
     * <br>
     * Ukaze formular pro modifikaci/vytvoreni CoffeeSite "coffeesite_create.html" se spravne vyplnenymi polozkami daneho CoffeeSite id.
     * 
     * @param id
     * @return
     */
    @GetMapping("/modifySite/{id}")
    public ModelAndView showSiteUpdatePage(@PathVariable(name = "id") Long id) {
        CoffeeSiteDTO cs = coffeeSiteService.findOneToTransfer(id);
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("coffeeSite", cs);
        mav.setViewName("coffeesite_create");
        return mav;
    }
   
    /**
     * Zpracuje POST request/formular ze stranky pro vytvareni/modifikaci CoffeeSite objektu.<br>
     * Po uspesnem zpracovani pozadavku na vytvoreni noveho CoffeeSite se zobrazi stranka coffeesite_detail,
     * ktera umozni aktivovat novy site nebo ho modifikovat.<br>
     * Po uspesnem zpracovani pozadavku na modifikaci CoffeeSite se zobrazi stejna stranka tj. umoznuje
     * provest dalsi modifikace na prave editovanem CoffeeSitu.
     * 
     * @param coffeeSite - novy/modifikovany objekt CoffeeSite z modelu, ktery vrati Spring/Thymeleaf, ktery ho vytvoril z formulare
     *                     pro vytvoreni/modifikaci CoffeeSite
     * @return
     */
    @PostMapping("/createModifySite") // Mapovani http POST na DB SAVE/UPDATE
    public String createOrUpdateCoffeeSite(@ModelAttribute("coffeeSite") @Valid CoffeeSiteDTO coffeeSite, final BindingResult bindingResult) {

        // Overeni, zda zadana pozice CoffeeSitu neni jiz pouzita.
        if (coffeeSite.getZemSirka() != null && coffeeSite.getZemDelka() != null) {
            if (coffeeSiteService.isLocationAlreadyOccupied(coffeeSite.getZemSirka(), coffeeSite.getZemDelka(), 5, coffeeSite.getId())) {
                bindingResult.rejectValue("zemSirka", "error.site.coordinate.latitude", "Location already occupied.");
                bindingResult.rejectValue("zemDelka", "error.site.coordinate.longitude", "Location already occupied.");
            }
        }
        
        if (bindingResult.hasErrors()) {
            return "coffeesite_create";
        }
        
        String returnView = "redirect:/showSite/";        
        String returnSuffix = "?createSuccess";
        
        if (coffeeSite.getId() != 0)
            returnSuffix = "?modifySuccess";
        
        CoffeeSite cs = coffeeSiteService.save(coffeeSite);
        
        if (cs != null)
            returnView = returnView + cs.getId() + returnSuffix;
        else
            returnView = "404";
        
        return returnView;
    }
    
    /**
     *  Zpracovani pozadavku na zmenu stavu CoffeeSite do stavu ACTIVE.<br>
     *  Pokud aktivaci provedl ADMIN, zobrazi se mu nasledni seznam vsech CoffeeSites,
     *  jinak se zobrazi seznam všech CoffeeSites, které vytvořil daný user. 
     */
    @PutMapping("/activateSite/{id}") 
    public String activateCoffeeSite(@PathVariable(name = "id") Long id) {
        // After CoffeeSite activation, go to the same page and show confirmation message
        CoffeeSite cs = coffeeSiteService.findOneById(id);
        cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, CoffeeSiteRecordStatusEnum.ACTIVE);
        return "redirect:/showSite/" + cs.getId() + "?activationSuccess";
    }
    
    @PutMapping("/deactivateSite/{id}") 
    public String deactivateCoffeeSite(@PathVariable(name = "id") Long id) {
        return modifyStatusAndReturnSameView(id, CoffeeSiteRecordStatusEnum.INACTIVE);
    }

    @PutMapping("/cancelStatusSite/{id}") 
    public String cancelStatusSite(@PathVariable(name = "id") Long id, RedirectAttributes redirectAttributes) {
        // ADMIN or DBA can still continue in CoffeeSite view page to modify site's status
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser.isPresent() &&  userService.hasADMINorDBARole(loggedInUser.get()))
            return modifyStatusAndReturnSameView(id, CoffeeSiteRecordStatusEnum.CANCELED);
        else // Normal USER is redirected to list of his/her sites after cancelling site
        {
            CoffeeSite cs = coffeeSiteService.findOneById(id);
            cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, CoffeeSiteRecordStatusEnum.CANCELED);
            String siteName = cs.getSiteName();
            redirectAttributes.addFlashAttribute("canceledSiteName", siteName);
            return "redirect:/mySites";
        }
    }

    /**
     * Pomocna metoda zdruzujici Controller prikazy pro některé modifikace stavu CoffeeSitu
     */
    private String modifyStatusAndReturnSameView(Long csID, CoffeeSiteRecordStatusEnum newStatus) {
        CoffeeSite cs = coffeeSiteService.findOneById(csID);
        cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, newStatus);
        return "redirect:/showSite/" + cs.getId();
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
        String siteName = coffeeSiteService.findOneById(id).getSiteName();
        redirectAttributes.addFlashAttribute("deletedSiteName", siteName);
        coffeeSiteService.delete(id);
        return "redirect:/allSites";
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