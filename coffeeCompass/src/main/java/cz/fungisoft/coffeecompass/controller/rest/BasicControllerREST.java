package cz.fungisoft.coffeecompass.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;
import io.swagger.annotations.Api;

/**
 * REST varianta zakladniho Controleru
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Swagger
@RestController
@RequestMapping("/rest") 
public class BasicControllerREST
{
    private StatisticsInfoService statsService;
    
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
