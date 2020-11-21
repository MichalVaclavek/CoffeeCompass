package cz.fungisoft.coffeecompass.dto;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import cz.fungisoft.coffeecompass.domain.weather.Main;
import cz.fungisoft.coffeecompass.domain.weather.Weather;
import lombok.Data;

/**
 * Selected Weather data to be added to CoffeeSiteDTO in CoffeeSiteService,
 * when CoffeeSiteDTO is requested by client.
 * 
 * @author Michal Vaclavek
 *
 */
@Data
public class WeatherDTO
{
    private List<Weather> weather = null;
    private Main main;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime sunRiseTime;
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime sunSetTime;
}
