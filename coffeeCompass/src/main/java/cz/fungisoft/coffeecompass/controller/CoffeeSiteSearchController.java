/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.fungisoft.coffeecompass.dto.CoffeeSortDTO;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import cz.fungisoft.coffeecompass.controller.models.CoffeeSiteSearchCriteriaModel;
import cz.fungisoft.coffeecompass.controller.models.OneStringModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.pojo.LatLong;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import lombok.extern.slf4j.Slf4j;

/**
 * Obsluha požadavků na stránce pro vyhledavani CoffeeSites a jejich zobrazovani v mape na coffeesites_search.html<br>
 * Stránka coffeesites_search.html obsahuje 2 formuláře, jeden pro vyhledávání CoffeeSites od souřadnic výchozího bodu<br>
 * na mapě (přesouvatelná značka na mapy.cz) a <br>
 * druhý, který vyhledává CoffeeSites ve městě, které zadá uživatel.<br><br>
 * Tento Controller obsluhuje Get požadavky z obou těchto formulářů a odkaz ze stránky home.html, který má zobrazit<br>
 * v mapě všechny CoffeeSites v daném městě. Jde o města, které se zobrazují ve statistice měst s největším počtem<br>
 * aktivních CoffeeSites. 
 * 
 * @author Michal Vaclavek
 */
@Controller
@Slf4j
public class CoffeeSiteSearchController {
    
    private static final String SEARCH_HTML_PAGE = "coffeesite_search_new";
    
    private static final String EMPTY_RESULT_MODEL_KEY = "emptyResult";
    
    private static final String FOUND_SITES_MODEL_KEY = "foundSites";
    
    private static final String SEARCH_CRITERIA_MODEL_KEY = "searchCriteria";
    
    
    private final CoffeeSiteService coffeeSiteService;
    
    private final CoffeeSortService coffeeSortService;
    
    
    /**
     * Patern pro povolene jmeno mesta. Alespon 2 znaky a oddelovace pro viceslovna jmena jako mezera, -, .
     */
    private final String allowedCityNamePattern = "^([\\p{IsAlphabetic}]{2})[\\p{IsAlphabetic}\\s-.,]+$";
    
    
    /**
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteSearchController(CoffeeSiteService coffeeSiteService
            , CoffeeSortService coffeeSortService) {
        super();
        this.coffeeSiteService = coffeeSiteService;
        this.coffeeSortService = coffeeSortService;
    }
    
    /**
     * Obsluha GET pozadavku z clienta pro zobrazeni stranky a formularu pro vyhledavani CoffeeSite, coffeesite_search.html<br>
     * Model obsahuje {@link CoffeeSiteSearchCriteriaModel} a jeho defaultni hodnoty<br>
     * a String model {@link OneStringModel} s poslednim jmenem mesta (currentSearchCity), ve kterem se pozadovalo vyhledavat.<br>
     * Defaultni pro polohu bude aktualni poloha zarizeni (pro mob. app.), pro webovou aplikaci je defaultni poloha stred CR.
     * 
     * @param model
     * @return
     */
    @GetMapping("/showSearch") // napr. http://localhost:8080/showSearch
    public String getSitesWithStatusAndCoffeeSort(Model model) {
        CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
        model.addAttribute(SEARCH_CRITERIA_MODEL_KEY, searchCriteria);
        return SEARCH_HTML_PAGE;
    }
    
   
    /**
     * Zpracovani GET pozadavku ze stranky/formulare pro vyhledavani CoffeeSites podle hodnot vyhledavacich atributu v<br>
     * CoffeeSiteSearchCriteria, ktere na strance vlozil uzivatel.<br>
     * Vraci stranku coffeesite_search.html se seznamem CoffeeSites, ktere vyhovuji vyhledavacim kriteriim, zobrazene v mape a v seznamu/tabulce.<br>
     * 
     * Must contain also model and BindingResult for the second form on the coffeesite_search.html page, otherwise Spring throws error.
     * 
     * @param searchCriteria - model vyhledavacich kriterii s daty ktere vlozil uzivatel a ktere do modelu spravne namapoval Thymeleaf 
     * @param bindingResultSearchCriteria - vysledek vytvoreni searchCriteria modelu z dat ve formulari pro vyhledani podle souradnic
     *  
     * @return vrati opet ModelAndView objekt pro vyhledavaci stranku, doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
     */
    @GetMapping("/searchSites") 
    public ModelAndView searchSitesWithStatusAndCoffeeSort(@ModelAttribute(SEARCH_CRITERIA_MODEL_KEY)
                                                           @Valid
                                                           CoffeeSiteSearchCriteriaModel searchCriteria,
                                                           final BindingResult bindingResultSearchCriteria) {
       
        ModelAndView mav = new ModelAndView();
        mav.setViewName(SEARCH_HTML_PAGE);
       
        if (bindingResultSearchCriteria.hasErrors()) {
            searchCriteria.resetSearchFromLocation();
            return mav;
        }
       
        String currentSearchCity = searchCriteria.getCityName();
       
        // Check if the cityName is valid city/town name
        // i.e. at least 2 characters at the beginning and " " (space) or "-" or "," are allowed in between.
        if (currentSearchCity.length() > 1 && !currentSearchCity.matches(allowedCityNamePattern)) {
            bindingResultSearchCriteria.rejectValue("cityName", "error.city.name.wrong", "Not a valid city name.");
            searchCriteria.resetSearchFromLocation();
            return mav;
        }
        
        // City name muze pochazet z mapy.cz a tedy obsahovat i oznaceni okresu a kraje.
        // pro vyhledavani v DB ale staci jen mesto , ktere je v tomto mapy.cz oznaceni pred prvni carkou
        int indexOfCarka = currentSearchCity.indexOf(",");
        if (indexOfCarka != -1) {
            currentSearchCity = currentSearchCity.substring(0, indexOfCarka);
        }
        
        List<CoffeeSiteDTO> foundSites;
       
        if (searchCriteria.getLat1() != null
            && searchCriteria.getLon1() != null
            && searchCriteria.getRange() != null
            && currentSearchCity.isEmpty()) {
            
            foundSites = coffeeSiteService.findAllWithinCircleAndCityWithCSStatusAndCoffeeSort(searchCriteria.getLat1(),
                                                                                               searchCriteria.getLon1(),
                                                                                               searchCriteria.getRange(),
                                                                                               searchCriteria.getCoffeeSiteStatus(),
                                                                                               currentSearchCity);
        } else {
            String encodedCityName = "";
            try {
                encodedCityName = URLEncoder.encode(currentSearchCity, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn("City name URL encoding error. City name '{}'.", currentSearchCity);
            }
            ModelAndView mavRedirect = new ModelAndView(new RedirectView("/searchSitesInCityForm/?cityName=" + encodedCityName + "&searchCityNameExactly=" + searchCriteria.isSearchCityNameExactly(), true));
           
            mavRedirect.addObject(SEARCH_CRITERIA_MODEL_KEY, searchCriteria);
            return mavRedirect;
        }
        
        if (foundSites != null && foundSites.isEmpty()) {
            mav.addObject(EMPTY_RESULT_MODEL_KEY, true); // nothing found, let to know to model
        }
        mav.addObject(FOUND_SITES_MODEL_KEY, foundSites);
        
        searchCriteria.setCityName(currentSearchCity); // set city name used for searching
        
        mav.addObject(SEARCH_CRITERIA_MODEL_KEY, searchCriteria);
        
        mav.setViewName(SEARCH_HTML_PAGE);
        return mav;
   }

   
   /**
    * Processes request to show one CoffeeSite in a map on the 'coffeesite_search.html' page, which then allows further searching.
    * 
    * @param siteExtId
    * @return
    */
   @GetMapping("/showSiteInMap/{siteExtId}") // napr. http://coffeecompass.cz/showSiteInMap/1fd1ea9f-f504-4331-ba91-04348f60948f
   public ModelAndView  showSiteInMap(@PathVariable String siteExtId) {
       ModelAndView mavRet = new ModelAndView(SEARCH_HTML_PAGE);
       
       return coffeeSiteService.findOneToTransfer(siteExtId).map(coffeeSite -> {
           CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
           List<CoffeeSiteDTO> foundSites = new ArrayList<>();

           foundSites.add(coffeeSite);
           LatLong searchFromLoc = coffeeSiteService.getSearchFromLocation(coffeeSite, 200);
           searchCriteria.setLon1(searchFromLoc.getLongitude());
           searchCriteria.setLat1(searchFromLoc.getLatitude());
           searchCriteria.setCityName(coffeeSite.getMesto());

           // To be visible in the table, which expects Page object
           Page<CoffeeSiteDTO> foundSitesPage = coffeeSiteService.getPageOfCoffeeSitesFromList(PageRequest.of(0, 1), foundSites);
           if (foundSitesPage != null) {
               mavRet.addObject("foundSitesPage", foundSitesPage);
           }

           mavRet.addObject(FOUND_SITES_MODEL_KEY, foundSites);
           mavRet.addObject(SEARCH_CRITERIA_MODEL_KEY, searchCriteria);
           return mavRet;
       }).orElseGet(() -> {
           mavRet.addObject(EMPTY_RESULT_MODEL_KEY, true);
           return mavRet;
       });
   }
   
   /**
    * Method to handle request to show all CoffeeSites of one city. This URI is used from home.html links of the list of "top" cities<br>
    * cityName should be always valid as it comes from href resp. from DB request output used for building the href on home.html links<br>
    * Returns ModelAndView for coffeesite_search.html page, but with redirection to "/searchSitesInCityForm" link, which serves like basic
    * entry point for "city" searching Form on 'coffeesite_search.html' page.
    * 
    * @param cityName - jmeno mesta z home.html ze statistiky mest s nejvetsim poctem aktivnich CoffeeSites
    * @return opet ModelAndView pro vyhledavaci stranku, s presmerovanim na "/searchSitesInCityForm" s parametry "cityName" a "searchCityNameExactly",
    * ktery urcuje, ze se CoffeeSites maji hledat, tak aby jejich atribut mesto byl presne shodny s "cityName"
    */
   @GetMapping("/showCitySites/") // napr. http://coffeecompass.cz/showCitySites/?cityName=Tišnov
   public ModelAndView  showCitySites(@RequestParam(value="cityName", defaultValue="") String cityName) {
       
       String encodedCityName = "";
        try {
            encodedCityName = URLEncoder.encode(cityName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("City name URL encoding error. City name '{}'.", cityName);
        }
        
       ModelAndView mav = new ModelAndView(new RedirectView("/searchSitesInCityForm/?cityName=" + encodedCityName + "&searchCityNameExactly=true", true));
       
       CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
       searchCriteria.setSearchCityNameExactly(true);
       mav.addObject(SEARCH_CRITERIA_MODEL_KEY, searchCriteria);
       
       return mav;
   }
   
   
   /**
    * Method to handle request to show all CoffeeSites of one city. Used to process a Form on coffeesite_search.html page or parametrized URL request.<br>
    * Must contain also model and BindingResult for the second form on the coffeesite_search.html page, otherwise Spring throws error.<br>
    * 
    * Returns ModelAndView for coffeesite_search.html page.
    * 
    * @param cityName - jmeno mesta, ve kterem se maji hledat CoffeeSites, inserted like URL parameter. Aktualne pouzito pri redirectu z predchozi metody resp. z URL "/showCitySites/?cityName=xxx" volane z home.html
    * @param searchExactly - parameter urcujici, zda se CoffeeSites maji hledat tak, ze jejich field "mesto" se presne shoduje s cityName nebo se maji hledat vsechny CoffeeSites, jejich filed mesto zacina retezcem "cityName"
    * 
    * @param searchCriteria - model vyhledavacich kriterii s daty, ktere vlozil uzivatel a ktere do modelu spravne namapoval Thymeleaf.<br>
    *        Zde se ale neoveruje, ani nevyuziva, protoze tento odkaz zpracovava pouze click na tlac. ve formulari pro vyhledavani podle jmena mesta.
    *        Pro spravnou funkcnost Springu je vsak treba uvest, protoze na strance "coffeesite_search.html" jsou 2 formulare se dvema modely.
    * @param bindingResultSearchCriteria - vysledek vytvoreni searchCriteria modelu z dat ve formulari pro vyhledani podle souradnic
    * 
    * @return opet ModelAndView pro vyhledavaci stranku, s presmerovanim na "/showCitySearch", doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
    */
   @GetMapping("/searchSitesInCityForm/") // napr.  http://coffeecompass.cz/searchSitesInCityForm/?cityName=Tišnov&searchCityNameExactly=true
   public ModelAndView  showCitySitesFromForm(@RequestParam(value="cityName", defaultValue="") String cityName,
                                              @RequestParam(value="searchCityNameExactly", defaultValue="false") boolean searchExactly,
                                              @ModelAttribute(SEARCH_CRITERIA_MODEL_KEY) CoffeeSiteSearchCriteriaModel searchCriteria,
                                              final BindingResult bindingResultSearchCriteria) {
       
       ModelAndView mav = new ModelAndView(SEARCH_HTML_PAGE);
       
       String currentSearchCity = cityName;
       
       if (currentSearchCity.isEmpty() && searchCriteria.getCityName() != null ) { // city name inserted by user in Form, not by calling /searchSitesInCityForm/?cityName=XXXX&searchCityNameExactly=true link
           currentSearchCity = searchCriteria.getCityName(); 
       } 
       
       // Check if the cityName is valid city/town name
       // i.e. at least 2 characters at the beginning and " " (space) or "-" or "," are allowed in between.
       if (currentSearchCity.length() > 1 && !currentSearchCity.matches(allowedCityNamePattern)) {
           bindingResultSearchCriteria.rejectValue("cityName", "error.city.name.wrong", "Not a valid city name.");
           return mav;
       }
       
       List<CoffeeSiteDTO> foundSites = new ArrayList<>();
       
       if (!currentSearchCity.isEmpty()) {
           foundSites = (searchExactly) ? coffeeSiteService.findAllByCityNameExactly(currentSearchCity)
                                        : coffeeSiteService.findAllByCityNameAtStart(currentSearchCity);
           
           if (foundSites == null || foundSites.isEmpty()) { // nothing found, let to know to model
               mav.addObject(EMPTY_RESULT_MODEL_KEY, true); 
           } else {
               searchCriteria = addAverageLocationToSearchCriteriaForFoundCoffeeSites(searchCriteria, foundSites);
           }
       }
       
       mav.addObject(FOUND_SITES_MODEL_KEY, foundSites);
       searchCriteria.setCityName(currentSearchCity);
       searchCriteria.setSearchCityNameExactly(searchExactly);
      
       mav.addObject(SEARCH_CRITERIA_MODEL_KEY, searchCriteria);
       
       return mav;
   }
   
   /**
    * Helper method.
    * Used for setting up searchcriteria model object (longitude and latitude) in case a list of CoffeeSites is found based on city name.
    * If {@code foundSites} are null, searchCriteria returns unchanged.
    * 
    * @return changed {@code searchCriteria} based on {@code foundSites}
    */
   private CoffeeSiteSearchCriteriaModel addAverageLocationToSearchCriteriaForFoundCoffeeSites(CoffeeSiteSearchCriteriaModel searchCriteria, List<CoffeeSiteDTO> foundSites) {
       if (foundSites != null) {
           if (foundSites.size() == 1) { // only one CoffeeSite found in city, define searchFrom point for this CoffeeSite
                LatLong searchFromLoc = coffeeSiteService.getSearchFromLocation(foundSites.get(0), 200);
                searchCriteria.setLon1(searchFromLoc.getLongitude());
                searchCriteria.setLat1(searchFromLoc.getLatitude());
           }
           else {
                /**
                 * Initial request to find CoffeeSites was not based on location (but the city name was the search parameter),
                 * therefore the CoffeeSiteSearchCriteriaModel must be filled-up using found CoffeeSites.
                 * Average longitude and latitude of found CoffeeSites is used.
                 */
                LatLong avgSitesLocation = coffeeSiteService.getAverageLocation(foundSites);
                searchCriteria.setLon1(avgSitesLocation.getLongitude());
                searchCriteria.setLat1(avgSitesLocation.getLatitude());
         }
     }
       
     return searchCriteria; 
   }
   
   
   /**
    * To be available for selection Coffee sort criteria in a View.
    * 
    * @return
    */
   @ModelAttribute("allCoffeeSorts")
   public List<CoffeeSortDTO> populateCoffeeSorts() {
       return coffeeSortService.getAllCoffeeSorts();
   }
}
