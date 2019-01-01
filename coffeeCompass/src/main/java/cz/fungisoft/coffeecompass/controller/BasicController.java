package cz.fungisoft.coffeecompass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;

/**
 * Controller pro obsluhu zakladnich odkazu.
 * <br>
 * Vrací základní html stránky uložené v src/main/resources/templates
 * 
 * @author Michal Vaclavek
 */
@Controller
public class BasicController
{   
    private StatisticsInfoService statsService;
    
    @Autowired
    public BasicController(StatisticsInfoService statsService) {
        super();
        this.statsService = statsService;
    }


    @GetMapping(value= {"/home", "/"})
    public ModelAndView home() {
        // Get and show statistical info
        ModelAndView mav = new ModelAndView("home");
        StatisticsToShow stats = statsService.getCurrentStatisticalInfoToShow();
        mav.addObject("stats", stats);
        return mav;
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "login";
    }
    
    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }
    
    @GetMapping("/404")
    public String error404() {
        return "error/404";
    }
    
    @GetMapping("/500")
    public String error500() {
        return "error/500";
    }

}
