package cz.fungisoft.coffeecompass.controller.rest;

import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import cz.fungisoft.coffeecompass.service.weather.WeatherApiService;

/**
 * Helper Controller to provide data about current weather on the CoffeeSite's geo coordinates.
 * Uses <a href="https://api.openweathermap.org">...</a> API. See {@link WeatherApiService} for more info.
 *
 * @author Michal Vaclavek
 *
 */
@Tag(name = "Coffee site Weather", description = "Get info about current weather on coffee site's location")
@RestController
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/weather")
public class WeatherControllerREST {
    
    private final WeatherApiService weatherService;

    public WeatherControllerREST(WeatherApiService weatherService) {
        super();
        this.weatherService = weatherService;
    }
    
    @GetMapping("/site/{extId}")
    @ResponseStatus(HttpStatus.OK)
    public WeatherDTO getWeatherInfoForSiteId(@PathVariable(value="extId") String extId) {
        return weatherService.getWeatherDTO(extId).orElseThrow(() -> new ResourceNotFoundException("Weather info", "coffeeSiteId", extId));
    }
}
