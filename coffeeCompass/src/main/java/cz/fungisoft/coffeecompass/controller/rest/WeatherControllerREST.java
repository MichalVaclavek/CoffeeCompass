package cz.fungisoft.coffeecompass.controller.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import cz.fungisoft.coffeecompass.service.WeatherApiService;
import io.swagger.annotations.Api;

@Api // Anotace Swagger
@RestController
@RequestMapping("/rest/weather") // vsechny http dotazy v kontroleru maji zacinat timto retezcem
public class WeatherControllerREST
{
    
    private WeatherApiService weatherService;

    public WeatherControllerREST(WeatherApiService weatherService) {
        super();
        this.weatherService = weatherService;
    }
    
    
    @GetMapping("/site/{id}")
    @ResponseStatus(HttpStatus.OK)
    public WeatherDTO getWeatherInfoForSiteId(@PathVariable(value="id") Long siteID) {
        
        return weatherService.getWeatherDTO(siteID);
    }

}