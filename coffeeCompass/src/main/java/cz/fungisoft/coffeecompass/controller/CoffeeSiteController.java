package cz.fungisoft.coffeecompass.controller;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteRecordStatus.CoffeeSiteRecordStatusEnum;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteStatus;
import cz.fungisoft.coffeecompass.entity.CoffeeSiteType;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.CupType;
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
import cz.fungisoft.coffeecompass.service.NextToMachineTypeService;
import cz.fungisoft.coffeecompass.service.OtherOfferService;
import cz.fungisoft.coffeecompass.service.PriceRangeService;
import cz.fungisoft.coffeecompass.service.SiteLocationTypeService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Základní Controller pro obsluhu požadavků, které se týkají práce s hlavním objektem CoffeeSite.<br>
 * Tj. pro základní CRUD operace a pro vyhledávání CoffeeSites.<br>
 * Tato verze je urcena pro pouziti se sablonovacim html/template systemem Thymeleaf, pro REST je vytvorena
 * extra verze CoffeeSiteRESTController.
 * <br>
 * 
 * @author Michal Václavek
 */
@Api // Anotace Swagger
@Controller // lepsi pro pouziti s sablonovacim systemem Thymeleaf, vraci stranky napr. coffeesite_create.html pri return "coffeesite_create";
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

    private CoffeeSiteService coffeeSiteService;
    
    /**
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.
     * 
     * Ale protoze techto servicu bude v tomto Controleru mnoho, bude konstruktor obsahovat pouze jeden
     * parametr se zakladnim Servicem CoffeeSiteService.
     * Ostatni service budou @Autowired jako atributy instance, Spring zaridi injection. Spravnejsi je pres setter
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteController(CoffeeSiteService coffeeSiteService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
    }

    /**
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
        
        User loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser != null &&  userService.hasADMINorDBARole(loggedInUser))
            mav.addObject("allSites", coffeeSiteService.findAll(orderBy, direction));  
        else
            mav.addObject("allSites", coffeeSiteService.findAllWithRecordStatus(CoffeeSiteRecordStatusEnum.ACTIVE));
        
        mav.setViewName("coffeesites_info");
    
        return mav;       
    }
    
    /**
     * Vrati stranku zobrazujici vsechny informace pro jeden CoffeeSite. Pokud je prihlaseny nejaky uzivatel,
     * zobrazi se moznost zadani hodnoceni (hvezdicky) a pridani komentare. Poku uzivatel zobrazi
     * site, ktery sam zalozil, zobrazuji se tlacitka pro zmenu stavu a pro Modifikaci)<br>
     * Model musi obsahovat polozky pro zadani hodnoceni (stars) a komentare.
     * 
     * @param id of CoffeeSite to show
     * @return
     */
    @GetMapping("/showSite/{id}") // napr. http://localhost:8080/showSite/2
    public ModelAndView showSite(@PathVariable int id) {
        
        ModelAndView mav = new ModelAndView();
        
        // Add CoffeeSite to model
        CoffeeSiteDto cs = coffeeSiteService.findOneToTransfer(id);
        
        mav.addObject("coffeeSite", cs);

        // Add object to cary users's comment and stars evaluation
        // If a user is logged-in, than find if he already saved comment for this site. If yes, this stars to model.
        StarsAndCommentModel starsAndComment = new StarsAndCommentModel();
        
        StarsQualityDescription userStarsForThisSite = starsForCoffeeSiteService.getStarsForCoffeeSiteAndLoggedInUser(cs);
        if (userStarsForThisSite != null )
            starsAndComment.setStars(userStarsForThisSite);
            
        mav.addObject("starsAndComment", starsAndComment);
        
        // Add comments for coffeeSite
        List<Comment> comments = commentsService.getAllCommentsForSiteId(id);
        mav.addObject("comments", comments);
        
        mav.setViewName("coffeesite_detail");
        
        return mav;
    }
    
    /**
     * Method to handle request to show CoffeeSites created by logged in user.
     * 
     * @param id
     * @return
     */
    @GetMapping("/mySites") // napr. http://localhost:8080/mySites
    public ModelAndView showMySites() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("allSites", coffeeSiteService.findAllFromLoggedInUser());
        mav.setViewName("coffeesites_info");
    
        return mav;   
    }
   
    /**
     * Zpracuje POST pozadavek ze stranky zobrazujici info o jednom CoffeeSite, z Formu, ktery umoznuje zadat
     * hodnoceni a komentar.
     * 
     * @param starsAndComment
     * @param id
     * @return
     */
    @PostMapping("/saveStarsAndComment/{id}") 
    public ModelAndView saveCommentAndStarsForSite(@ModelAttribute StarsAndCommentModel starsAndComment, @PathVariable int id) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSite(id, starsAndComment.getStars().getNumOfStars());
        
        CoffeeSite cs = coffeeSiteService.findOneById(id);
        
        if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty())
            commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
        
        // Show same coffee site with new Stars and comments
        CoffeeSiteDto cst = coffeeSiteService.findOneToTransfer(id);
        ModelAndView mav = new ModelAndView("redirect:/showSite/"+ cst.getId());
        
        return mav;
    }
   
    @GetMapping("/site/") // napr. http://localhost:8080/site/?name=test1
    public CoffeeSiteDto siteByName(@RequestParam(value="name") String name) {
        return coffeeSiteService.findByName(name);
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
    @GetMapping("/dist/") // napr. http://localhost:8080/dist/?lat1=50.235&lon1=14.235&lat2=50.335&lon2=14.335
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
    public ModelAndView siteToCreateAndModify(final CoffeeSiteDto coffeeSite) {
        
        CoffeeSiteDto cs;
        
        cs = coffeeSite; // novou instanci CoffeeSite vytvori Spring ? v konstruktoru
        cs.setId(0); // je potreba zadat ID=0, aby se v Thymeleaf formulari dalo rozlisit, ze jde o novy CoffeeSite a zobrazit spravny Label text aj.
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
    public ModelAndView showSiteUpdatePage(@PathVariable(name = "id") Integer id) {
//        CoffeeSite cs = coffeeSiteService.findOneById(id);
        CoffeeSiteDto cs = coffeeSiteService.findOneToTransfer(id);
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("coffeeSite", cs);
        mav.setViewName("coffeesite_create");
        return mav;
    }
   
    /**
     * Zpracuje POST request/formular ze stranky pro vytvareni/modifikaci CoffeeSite objektu.<br>
     * Po uspesnem zpracovani pozadavku na vytvoreni noveho CoffeeSite se zobrazi stranka coffeesite_detail,
     * ktera umozni aktivovat novy site nebo ho modifikovat.<br>
     * Po uspesnem zpracovani pozadavku na modifikaci CoffeeSite se zobrazi stejna stranka tj. umozn
     * provest dalsi modifikace na prave editovanem CoffeeSitu.
     * 
     * @param coffeeSite - novy/modifikovany objekt CoffeeSite z modelu, ktery vrati Spring, ktery ho vytvoril z formulare
     * pro vytvoreni/modifikaci CoffeeSite
     * @return
     */
    @PostMapping("/createModifySite") // Mapovani http POST na DB SAVE/UPDATE
    public String createOrUpdateCoffeeSite(@ModelAttribute("coffeeSite") @Valid CoffeeSiteDto coffeeSite, final BindingResult bindingResult) {
        //Overeni jmena, nesmi se shodovat s jinym jmenem
        if (!coffeeSiteService.isSiteNameUnique(coffeeSite.getId(), coffeeSite.getSiteName())) {
            bindingResult.rejectValue("siteName", "error.site.name.used", "Name already used.");
        }
        
        if (bindingResult.hasErrors()) {
            //TODO nebylo by lepsi udelat pomoci redirect:\ ...
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
    
    /* *** Zpracovani pozadavku na zmenu stavu CoffeeSite *** */
   
    @PutMapping("/activateSite/{id}") 
    public String activateCoffeeSite(@PathVariable(name = "id") Integer id) {
        // After CoffeeSite activation, go to My Sites list
        CoffeeSite cs = coffeeSiteService.findOneById(id);
        cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, CoffeeSiteRecordStatusEnum.ACTIVE);

        return userService.isADMINloggedIn() ? "redirect:/allSites" : "redirect:/mySites";
    }
    
    @PutMapping("/deactivateSite/{id}") 
    public String deactivateCoffeeSite(@PathVariable(name = "id") Integer id) {
        return modifyStatusAndReturnSameView(id, CoffeeSiteRecordStatusEnum.INACTIVE);
    }

    @PutMapping("/cancelStatusSite/{id}") 
    public String cancelStatusSite(@PathVariable(name = "id") Integer id) {
        // ADMIN or DBA can still continue in CoffeeSite view page to modify site's status
        User loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser != null &&  userService.hasADMINorDBARole(loggedInUser))
            return modifyStatusAndReturnSameView(id, CoffeeSiteRecordStatusEnum.CANCELED);
        else // Normal USER is redirected to list of his/her sites
        {
            CoffeeSite cs = coffeeSiteService.findOneById(id);
            cs = coffeeSiteService.updateCSRecordStatusAndSave(cs, CoffeeSiteRecordStatusEnum.CANCELED);
            String returnView = "redirect:/mySites";
            
            return returnView;
        }
    }

    /**
     * Pomocna metoda zdruzujici Controller prikazy pro modifikaci stavu CoffeeSitu
     */
    private String modifyStatusAndReturnSameView(Integer csID, CoffeeSiteRecordStatusEnum newStatus) {
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
    public String finalDelete(@PathVariable int id) {
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