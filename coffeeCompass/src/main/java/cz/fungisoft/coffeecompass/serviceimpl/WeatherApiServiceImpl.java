package cz.fungisoft.coffeecompass.serviceimpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.domain.weather.WeatherData;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.WeatherDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.WeatherApiService;
import ma.glasnost.orika.MapperFacade;

/**
 * Implementation of the {@link WeatherApiService} using 
 * https://api.openweathermap.org/data/2.5/weather API.
 * 
 * @author Michal Vaclavek
 *
 */
@Service
public class WeatherApiServiceImpl implements WeatherApiService
{

    private final String WEATHER_API_URL;
    
    private final String APP_ID;
    
    private MapperFacade mapperFacade;
    
    private CoffeeSiteService coffeeSiteService;

    public WeatherApiServiceImpl( @Value("${weather.api.url}") String api_url, @Value("${weather.api.id}") String api_id,
                                  MapperFacade mapperFacade, CoffeeSiteService coffeeSiteService) {
        //this.restTemplate = restTemplate;
        this.WEATHER_API_URL = api_url;
        this.APP_ID = api_id;
        this.mapperFacade = mapperFacade;
        this.coffeeSiteService = coffeeSiteService;
    }
    
    @Override
    public WeatherData getWeather(Double lat, Double lon, String lang, String units) {
        
        RestTemplate restTemplate = new RestTemplate();
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(WEATHER_API_URL)
                .queryParam( "lon", lon)
                .queryParam( "lat", lat)
                .queryParam( "APPID", APP_ID)
                .queryParam( "lang", lang)
                .queryParam( "units", units);

        WeatherData jsonNode = restTemplate.getForObject(uriBuilder.toUriString(), WeatherData.class);
        
        return jsonNode;

    }

    @Override
    public WeatherDTO getWeatherDTO(CoffeeSiteDTO coffeeSite) {
        WeatherData weather = getWeather(coffeeSite.getZemSirka(), coffeeSite.getZemDelka(), "cz", "metric");
        return mapperFacade.map(weather, WeatherDTO.class);
    }

    @Override
    public WeatherDTO getWeatherDTO(Long coffeeSiteId) {
        
        CoffeeSite coffeeSite = coffeeSiteService.findOneById(coffeeSiteId);
        WeatherData weather = getWeather(coffeeSite.getZemSirka(), coffeeSite.getZemDelka(), "cz", "metric");
        return mapperFacade.map(weather, WeatherDTO.class);
    }

}
