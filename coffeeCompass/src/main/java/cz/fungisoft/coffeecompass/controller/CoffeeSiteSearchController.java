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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.controller.models.CoffeeSiteSearchCriteriaModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSort;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.CoffeeSortService;
import io.swagger.annotations.Api;

/**
 * Obsluha pozadavku na strance pro vyhledavani CoffeeSites.
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
     * Obsluha GET pozadavku z clienta pro zobrazeni stranky/formulare pro vyhledavani CoffeeSite.
     * Model obsahuje CoffeeSiteSearchCriteria a jeho defaultni hodnoty. Defaultni pro polohu bude aktualni poloha zarizeni.
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
     * Zpracovani GET pozadavku ze stranky/formulare pro vyhledavani CoffeeSites podle hodnot vyhledavacich atributu
     * CoffeeSiteSearchCriteria, ktere na strance vlozil uzivatel.<br>
     * Vraci stranky se seznamem CoffeeSites, ktere vyhovuji vyhledavacim kriteriim.
     * 
     * @param searchCriteria
     * @return
     */
   @GetMapping("/searchSites") // napr. http://localhost:8080/getSites/?lat1=50.1669497&lon1=14.7711140&range=50000&status=Opened&sort=espresso
   public ModelAndView searchSitesWithStatusAndCoffeeSort(@ModelAttribute("searchCriteria") @Valid CoffeeSiteSearchCriteriaModel searchCriteria, final BindingResult bindingResult) {
       
       ModelAndView mav = new ModelAndView();
       
       if (bindingResult.hasErrors()) {
           mav.setViewName("coffeesite_search");
           return mav;
       }
       // CoffeeSort is not intended to be filter now
       if (!searchCriteria.getSortSelected())
           searchCriteria.setCoffeeSort("");
           
       List<CoffeeSiteDTO> foundSites = coffeeSiteService.findAllWithinCircleWithCSStatusAndCoffeeSort(searchCriteria.getLat1(),
                                                                                                        searchCriteria.getLon1(),
                                                                                                        searchCriteria.getRange(),
                                                                                                        searchCriteria.getCoffeeSort(),
                                                                                                        searchCriteria.getCoffeeSiteStatus());
        
       mav.addObject("foundSites", foundSites);
       if (foundSites == null || foundSites.size() == 0)
           mav.addObject("emptyResult", true);
        
       searchCriteria.setSortSelected(false); // set deault value before next searching
        
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
