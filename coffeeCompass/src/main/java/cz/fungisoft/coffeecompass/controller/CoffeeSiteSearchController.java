/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.controller.models.CoffeeSiteSearchCriteriaModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import io.swagger.annotations.Api;

/**
 * Obsluha požadavků na stránce pro vyhledavani CoffeeSites a jejich zobrazovani v mape, coffeesites_search.html
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
     * Obsluha GET pozadavku z clienta pro zobrazeni stranky/formulare pro vyhledavani CoffeeSite, coffeesite_search.html<br>
     * Model obsahuje {@link CoffeeSiteSearchCriteriaModel} a jeho defaultni hodnoty.<br>
     * Defaultni pro polohu bude aktualni poloha zarizeni, pro webovou aplikaci je defaultni poloha stred CR.
     * 
     * @param model
     * @return
     */
    @GetMapping("/showSearch") // napr. http://localhost:8080/showSearch
    public String getSitesWithStatusAndCoffeeSort(Model model) {
        CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
            
        model.addAttribute("searchCriteria", searchCriteria);
        return "coffeesite_search";
    }
    
   
    /**
     * Zpracovani GET pozadavku ze stranky/formulare pro vyhledavani CoffeeSites podle hodnot vyhledavacich atributu v
     * CoffeeSiteSearchCriteria, ktere na strance vlozil uzivatel.<br>
     * Vraci stranku coffeesite_search.html se seznamem CoffeeSites, ktere vyhovuji vyhledavacim kriteriim, zobrazene v mape a v seznamu/tabulce.
     * 
     * @param searchCriteria - model vyhledavacich kriterii s daty ktere vlozil uzivatel a ktere do modelu spravne namapoval Thymeleaf 
     * @return vrati opet model pro vyhledavaci stranku, doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
     */
   @GetMapping("/searchSites") // napr. http://localhost:8080/getSites/?lat1=50.1669497&lon1=14.7711140&range=50000&status=Opened&sort=espresso
   public ModelAndView searchSitesWithStatusAndCoffeeSort(@ModelAttribute("searchCriteria") @Valid CoffeeSiteSearchCriteriaModel searchCriteria, final BindingResult bindingResult) {
       
       ModelAndView mav = new ModelAndView();
       
       if (bindingResult.hasErrors()) {
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
        
       mav.addObject("foundSites", foundSites);
       if (foundSites == null || foundSites.size() == 0)
           mav.addObject("emptyResult", true); // nothing found, let to know to model
        
       searchCriteria.setSortSelected(false); // set deault value before next searching
        
       mav.setViewName("coffeesite_search");
       return mav;
   }
   
   /**
    * Method to handle request to show all CoffeeSites of one city. Basicly used from home.html links<br>
    * and from coffeesite_search.html pages
    * 
    * @return vrati opet model pro vyhledavaci stranku, doplneny o vyhledane CoffeeSites nebo o "flag", ktery oznacuje, ze nebylo nalezeno nic
    */
   @GetMapping("/showCitySites/") // napr. http://coffeecompass.cz/?cityName=Tišnov
   public ModelAndView showCitySites(@RequestParam(value="cityName") String cityName) {
       
       ModelAndView mav = new ModelAndView();
       List<CoffeeSiteDTO> foundSites = coffeeSiteService.findByCityName(cityName);
       CoffeeSiteSearchCriteriaModel searchCriteria = new CoffeeSiteSearchCriteriaModel();
       
       if (foundSites != null) {
           if ( foundSites.size() == 0) {
               mav.addObject("emptyResult", true); // nothing found, let to know to model
           } else {
               /**
                * Initial request to find CoffeeSites was not based on location, therefore the search criteria model
                * must be filled-up using found CoffeeSites. Average longitude and latitude of found CoffeeSites is used.
                */
               searchCriteria.setLon1(coffeeSiteService.getAverageLocation(foundSites).getLongitude());
               searchCriteria.setLat1(coffeeSiteService.getAverageLocation(foundSites).getLatitude());
           }
       }
       
       mav.addObject("foundSites", foundSites);
       mav.addObject("searchCriteria", searchCriteria);
       mav.setViewName("coffeesite_search");
   
       return mav;   
   }
   
   /**
    * To be available for selection Coffee sort criteria in a View
    * 
    * @return
    */
   @ModelAttribute("allCoffeeSorts")
   public List<CoffeeSort> populateCoffeeSorts() {
       return coffeeSortService.getAllCoffeeSorts();
   }
   
}
