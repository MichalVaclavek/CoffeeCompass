package cz.fungisoft.coffeecompass.controller.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;

/**
 * REST varianta zakladniho Controleru
 * 
 * @author Michal Vaclavek
 */
@Tag(name = "Basic pages", description = "Home page")
@RestController
@RequestMapping("${site.coffeesites.baseurlpath.rest}")
public class BasicControllerREST {

    private final StatisticsInfoService statsService;
    
    @Autowired
    public BasicControllerREST(StatisticsInfoService statsService) {
        super();
        this.statsService = statsService;
    }


    @GetMapping(value= {"/home", "/"})
    @ResponseStatus(HttpStatus.OK)
    public StatisticsToShow home() {
        // Get and show statistical info
        StatisticsToShow stats = statsService.getCurrentStatisticalInfoToShow();
        return stats;
    }
}
