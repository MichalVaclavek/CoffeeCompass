/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import cz.fungisoft.coffeecompass.controller.models.CoffeeSiteSearchCriteriaModel;
import cz.fungisoft.coffeecompass.controller.models.OneStringModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import io.swagger.annotations.Api;

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
@Api // Anotace Swagger
@Controller
public class CoffeeSiteSearchController
{
    private CoffeeSiteService coffeeSiteService;
    
    private CoffeeSortService coffeeSortService;
    
    /**
     * To save current city for search CoffeeSites in, which is inserted by Form on coffeesites_search.html
     * or by link on home.html (link on statistics list of cities with most number of active CoffeeSites).
     * Needed for processing Get requests with validation error of city name 
     */
    private String currentSearchCity = "";
    private boolean searchCityExactly = false; // default mode for searching according city name
    
    /**
     * Dependency Injection pomoci konstruktoru, neni potreba uvadet @Autowired u atributu, Spring toto umi automaticky.
     * Lze ale uvest u konstruktoru, aby bylo jasne, ze Injection provede Spring.
     * 
     * @param coffeeSiteService
     */
    @Autowired
    public CoffeeSiteSearchController(CoffeeSiteService coffeeSiteService, CoffeeSortService coffeeSortService) {
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
            
        model.addAttribute("searchCriteria", searchCriteria);
        
        OneStringModel cityName = new OneStringModel();
        cityName.setInput(currentSearchCity);
        
        model.addAttribute("cityName", cityName);
            
        return "coffeesite_search";
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
     * @param cityName - model pro vyhledavani podle mesta. Obsahuje pouze retezec se jmenem mesta.<br>
     *                   Zde se ale neoveruje, ani nevyuziva, protoze tento odkaz zpracovava pouze click na tlac. ve formulari pro vyhledavani podle souradnic.
     * @param bindingResultCityName - vysledek vytvoreni OneStringModel modelu z dat ve formulari pro vyhledani podle jmena mesta
     *  
     * @return vrati opet ModelAndView objekt pro vyhledavaci stranku, doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
     */
   @GetMapping("/searchSites") 
   public ModelAndView searchSitesWithStatusAndCoffeeSort(@ModelAttribute("searchCriteria") @Valid CoffeeSiteSearchCriteriaModel searchCriteria, final BindingResult bindingResultSearchCriteria,
                                                          @ModelAttribute("cityName") OneStringModel cityName, final BindingResult bindingResultCityName) {
       
       ModelAndView mav = new ModelAndView();
       
       if (bindingResultSearchCriteria.hasErrors()) {
           mav.setViewName("coffeesite_search");
           return mav;
       }
       // CoffeeSort is not intended to be in filter now
       if (!searchCriteria.getSortSelected()) {
           searchCriteria.setCoffeeSort("");
       }
           
       List<CoffeeSiteDTO> foundSites = coffeeSiteService.findAllWithinCircleWithCSStatusAndCoffeeSort(searchCriteria.getLat1(),
                                                                                                       searchCriteria.getLon1(),
                                                                                                       searchCriteria.getRange(),
                                                                                                       searchCriteria.getCoffeeSort(),
                                                                                                       searchCriteria.getCoffeeSiteStatus());
        
       //TODO handle the case that only one CoffeeSite was found. In such case, the searchCriteria lat. , long. must be changed
       mav.addObject("foundSites", foundSites);
       if (foundSites == null || foundSites.size() == 0)
           mav.addObject("emptyResult", true); // nothing found, let to know to model
        
       searchCriteria.setSortSelected(false); // set deault value before next searching
        
       mav.setViewName("coffeesite_search");
       return mav;
   }
   
   
   /**
    * Method to handle request to show all CoffeeSites of one city. Basicaly used from home.html links of the list of "top" cities<br>
    * cityName should be always valid as it comes from href resp. from DB request output used for building respective href on home.html links<br>
    * Returns ModelAndView for coffeesite_search.html page, but with redirection to "/showCitySearch" link, which serves like basic entry point in case of refreshing the page or
    * during language swap.
    * 
    * @param cityName - jmeno mesta z home.html ze statistiky mest s nejvetsim poctem aktivnich CoffeeSites
    * @param redirectAttributes - parametr nutny pro vlozeni prislusnych atributu modelu, ktere se maji prenest pri redirection.
    * @return opet ModelAndView pro vyhledavaci stranku, s presmerovanim na "/showCitySearch", doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
    */
   @GetMapping("/showCitySites/") // napr. http://coffeecompass.cz/showCitySites/?cityName=Tišnov
   public ModelAndView  showCitySites(@RequestParam(value="cityName") String cityName, RedirectAttributes redirectAttributes) {
       return redirectAndShowSitesInCity(cityName, true,  redirectAttributes);
   }
   
   /**
    * Method to handle request to show all CoffeeSites of one city. Used to process a Form on coffeesite_search.html page.<br>
    * Must contain also model and BindingResult for the second form on the coffeesite_search.html page, otherwise Spring throws error.<br>
    * 
    * Returns ModelAndView for coffeesite_search.html page, but with redirection to "/showCitySearch" link, which serves like basic entry point in case of refreshing the page or
    * during language swap.
    * 
    * @param searchCriteria - model vyhledavacich kriterii s daty ktere vlozil uzivatel a ktere do modelu spravne namapoval Thymeleaf.<br>
    *        Zde se ale neoveruje, ani nevyuziva, protoze tento odkaz zpracovava pouze click na tlac. ve formulari pro vyhledavani podle jmena mesta. 
    * @param bindingResultSearchCriteria - vysledek vytvoreni searchCriteria modelu z dat ve formulari pro vyhledani podle souradnic
    * @param cityName - model pro vyhledavani podle mesta. Obsahuje pouze retezec se jmenem mesta. 
    * @param bindingResultCityName - vysledek vytvoreni OneStringModel modelu z dat ve formulari pro vyhledani podle jmena mesta
    * 
    * @param redirectAttributes - parametr nutny pro vlozeni prislusnych atributu modelu, ktere se maji prenest pri redirection.
    * 
    * @return opet ModelAndView pro vyhledavaci stranku, s presmerovanim na "/showCitySearch", doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
    */
   @GetMapping("/searchSitesInCityForm") 
   public ModelAndView  showCitySitesFromForm(@ModelAttribute("cityName") OneStringModel cityName, final BindingResult bindingResultCityName,
                                              @ModelAttribute("searchCriteria") CoffeeSiteSearchCriteriaModel searchCriteria, final BindingResult bindingResultSearchCriteria,
                                              RedirectAttributes redirectAttributes) {
       
       ModelAndView mav = new ModelAndView("coffeesite_search");
       
       if (cityName.getInput() != null) {
           currentSearchCity = cityName.getInput(); // currentSearchCity is used in case the page is refreshed when selecting another language and current url is still valid i.e. in case of previous validation failure
       } else
           cityName.setInput(currentSearchCity);
       
       if (bindingResultCityName.hasErrors()) {
           return mav;
       } else { // Check if the cityName is valid city/town name
                // i.e. at least 2 characters at the beginning and " " (space) or "-" allowed in between.
           if (!currentSearchCity.matches("^([\\p{IsAlphabetic}]{2})[\\p{IsAlphabetic} -]+$")) {
               bindingResultCityName.rejectValue("input", "error.city.name.wrong", "Not valid city name.");
               return mav;
           }
       }
       
       return redirectAndShowSitesInCity(currentSearchCity, false, redirectAttributes);
   }
   
   /**
    * Pomocna metoda, ktera zdruzuje spolecne akce pri presmerovani z odkazu vyse "/searchSitesInCityForm" a "/showCitySites/"
    * Preda atributy nutne pro vyhledani podle jmena mesta ze dvou ruznych odkazu, tj. jmeno mesta a zpusob hledani podle mesta.
    * 
    * @param cityName - jmeno mesta, kde se maji hledat CoffeeSites
    * @param searchExactly - jak se maji hledat CoffeeSites - dana polzka "mesto" daneho CoffeeSite se musi presne shodovat s cityName (true)
    *                       nebo se hledaji vsechny CoffeeSites, jejich field "mesto" zacina na  cityName (false)
    * @param redirectAttributes - object pro ulozeni parametru, ktere se maji prenest do dalsi metody, ktere zpracovavaji presmerovany odkaz "/showCitySearch"
    * 
    * @return  ModelAndView which performs redirection to "/showCitySearch"
    */
   private ModelAndView  redirectAndShowSitesInCity(String cityName, boolean searchExactly, RedirectAttributes redirectAttributes) {
       ModelAndView mav = new ModelAndView("redirect:/showCitySearch");
       
       redirectAttributes.addFlashAttribute("cityNameString", cityName);
       redirectAttributes.addFlashAttribute("searchExactlyCityName", searchExactly);
       
       return mav;
   }
   
   /**
    * Method to process redirected GET request from:<br>
    * 
    * {@link #showCitySitesFromForm()} and <br>
    * {@link #showCitySites()} <br>
    * 
    * methods.
    * <br>
    * Creates and returns correct ModelMap for both Forms on coffeesite_search.html with "List<CoffeeSiteDTO> foundSites" created according requests on<br>
    * "/searchSitesInCityForm" and  "/showCitySites/" links i.e. according city name.<br>
    * If the "/searchSitesInCityForm" was invoked, then all the CoffeeSites whose "mesto" begins with "currentSearchCity" are found,<br>
    * if the "/showCitySites/" was invoked, then all the CoffeeSites whose "mesto" is exactly equal to "currentSearchCity" are found.<br>
    * Can be invoked repeatable (in case of refresh or language change) as it saves current city name and "mode" for searching CoffeeSites in city.
    * 
    * @param model - ModelMap for coffeesite_search.html page. Contains CoffeeSites to be shown on a map and all attributes for both Forms on the coffeesite_search.html page
    * @return vrati opet model pro vyhledavaci stranku, doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic.
    */
   //@ModelAttribute("cityNameString") Object cityName
   @GetMapping("/showCitySearch") 
   private ModelAndView  createModelAndShowSitesInCity(ModelMap model) {
       
       ModelAndView mav = new ModelAndView();
       
       CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
       
       List<CoffeeSiteDTO> foundSites = null;
       
       // City name and search mode are global variables for this Controller as it needs to be saved in case
       // the search page is refreshed or lang is changed
       searchCityExactly = (boolean) model.getOrDefault("searchExactlyCityName", searchCityExactly);
       currentSearchCity = (String) model.getOrDefault("cityNameString", currentSearchCity);
       
       if (searchCityExactly) {
           foundSites = coffeeSiteService.findAllByCityNameExactly(currentSearchCity);
       } else {
           foundSites = coffeeSiteService.findAllByCityNameAtStart(currentSearchCity);
       }
       
       //TODO handle the case that only one CoffeeSite was found. In such case, the searchCriteria lat. , long. must be changed
       
       if (foundSites != null) {
           if ( foundSites.size() == 0) {
               mav.addObject("emptyResult", true); // nothing found, let to know to model
           } else {
               /**
                * Initial request to find CoffeeSites was not based on location (city name was the search parameter),
                * therefore the CoffeeSiteSearchCriteriaModel must be filled-up using found CoffeeSites.
                * Average longitude and latitude of found CoffeeSites is used.
                */
               searchCriteria.setLon1(coffeeSiteService.getAverageLocation(foundSites).getLongitude());
               searchCriteria.setLat1(coffeeSiteService.getAverageLocation(foundSites).getLatitude());
           }
       }
       
       mav.addObject("foundSites", foundSites);
       mav.addObject("searchCriteria", searchCriteria);
       
       OneStringModel cityNameModel = new OneStringModel();
       cityNameModel.setInput(currentSearchCity);
       mav.addObject("cityName", cityNameModel);
       
       mav.setViewName("coffeesite_search");
       
       return mav;
   }
   
   
   /**
    * To be available for selection Coffee sort criteria in a View.
    * 
    * @return
    */
   @ModelAttribute("allCoffeeSorts")
   public List<CoffeeSort> populateCoffeeSorts() {
       return coffeeSortService.getAllCoffeeSorts();
   }
   
}
