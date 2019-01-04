package cz.fungisoft.coffeecompass.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;
import io.swagger.annotations.Api;

@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest") // uvadi se, pokud vsechny dotazy v kontroleru maji zacinat timto retezcem
public class BasicControllerREST
{
    private StatisticsInfoService statsService;
    
    @Autowired
    public BasicControllerREST(StatisticsInfoService statsService) {
        super();
        this.statsService = statsService;
    }


    @GetMapping(value= {"/home", "/"})
    public ResponseEntity<StatisticsToShow> home() {
        // Get and show statistical info
        StatisticsToShow stats = statsService.getCurrentStatisticalInfoToShow();
        return new ResponseEntity<StatisticsToShow>(stats, HttpStatus.OK);
    }
}
