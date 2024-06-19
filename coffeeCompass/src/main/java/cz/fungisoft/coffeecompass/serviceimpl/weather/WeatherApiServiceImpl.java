package cz.fungisoft.coffeecompass.serviceimpl.weather;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.mappers.WeatherMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.domain.weather.WeatherData;
import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.weather.WeatherApiService;

import java.util.Optional;

/**
 * Implementation of the {@link WeatherApiService} using 
 * https://api.openweathermap.org/data/2.5/weather API.
 * 
 * @author Michal Vaclavek
 *
 */
@Service
public class WeatherApiServiceImpl implements WeatherApiService {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiServiceImpl.class);

    private final String WEATHER_API_URL;
    
    private final String APP_ID;
    
    private final WeatherMapper weatherMapper;
    
    private final CoffeeSiteService coffeeSiteService;

    public WeatherApiServiceImpl( @Value("${weather.api.url}") String apiUrl, @Value("${weather.api.id}") String apiId,
                                  WeatherMapper weatherMapper,
                                  CoffeeSiteService coffeeSiteService) {
        this.WEATHER_API_URL = apiUrl;
        this.APP_ID = apiId;
        this.weatherMapper = weatherMapper;
        this.coffeeSiteService = coffeeSiteService;
    }
    
    @Override
    public Optional<WeatherData> getWeather(Double lat, Double lon, String lang, String units) {
        
        RestTemplate restTemplate = new RestTemplate();
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(WEATHER_API_URL)
                .queryParam( "lon", lon)
                .queryParam( "lat", lat)
                .queryParam( "APPID", APP_ID)
                .queryParam( "lang", lang)
                .queryParam( "units", units);
        Optional<WeatherData> jsonNode = Optional.empty();
        try {
            WeatherData jsonResponse = restTemplate.getForObject(uriBuilder.toUriString(), WeatherData.class);
            jsonNode = Optional.ofNullable(jsonResponse);
        }
        catch (Exception ex) {
            log.error("Error retrieving weather information: {}", ex.getMessage());
        }
        
        return jsonNode;

    }

    @Override
    public Optional<WeatherDTO> getWeatherDTO(CoffeeSite coffeeSite) {
        Optional<WeatherData> weather = getWeather(coffeeSite.getZemSirka(), coffeeSite.getZemDelka(), "cz", "metric");
        return weather.map(weatherMapper::weatherDataToWeatherDTO);
    }

    @Override
    public Optional<WeatherDTO> getWeatherDTO(Long coffeeSiteId) {
        return coffeeSiteService.findOneById(coffeeSiteId)
                                .flatMap(coffeeSite -> getWeather(coffeeSite.getZemSirka(), coffeeSite.getZemDelka(), "cz", "metric"))
                                .map(weatherMapper::weatherDataToWeatherDTO);
    }
}
