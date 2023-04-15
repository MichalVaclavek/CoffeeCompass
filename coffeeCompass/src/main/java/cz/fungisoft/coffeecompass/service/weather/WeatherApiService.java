package cz.fungisoft.coffeecompass.service.weather;

import cz.fungisoft.coffeecompass.domain.weather.WeatherData;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.WeatherDTO;

import java.util.Optional;

/**
 * Service interface to declare Weather service.
 * <p>
 * Returned Weather info can be then used as additional info on the page, where CoffeeSite's details are shown.
 * Can be also used in REST API as return data per individual request. 
 * <p>
 * Expected implementation is to read weather data from https://api.openweathermap.org/data/2.5/weather
 * using {@code RestTempalte}. 
 * 
 * @author Michal Vaclavek
 *
 */
public interface WeatherApiService {
    
    /**
     * Gets weather information for given geo coordinates, language and units (default 'metric' i.e. Celsius degree)
     * 
     * @param lat - geo latitude
     * @param lon - geo longitude
     * @param lang - language abbrevation like 'cz' or 'en'
     * @param units - usually 'metric' i.e. Celsius degree are return as temperature values
     * @return
     */
    Optional<WeatherData>  getWeather(Double lat, Double lon, String lang, String units);
    
    /**
     * Gets weather information for given CoffeeSite's geo coordinations, language 'cz' and 'metric' units  i.e. Celsius degree,
     * to be sent to client. Not all {@link WeatherData} needs to be returned, so filtering to {@link WeatherDTO} is performed here.
     * 
     * @param coffeeSite who's geo coordinations are used as input data for obtaining current weather info from openweathermap.com
     * @return
     */
    Optional<WeatherDTO> getWeatherDTO(CoffeeSiteDTO coffeeSite);
    
    /**
     * Gets weather information for given CoffeeSite's geo coordinations, language 'cz' and 'metric' units  i.e. Celsius degree,
     * to be sent to client. Not all {@link WeatherData} needs to be returned, so filtering to {@link WeatherDTO} is performed here.
     * 
     * @param coffeeSiteId - id of the CoffeeSite who's geo coordinations are used as input data for obtaining current weather info from openweathermap.com
     * @return
     */
    Optional<WeatherDTO> getWeatherDTO(Long coffeeSiteId);
}
