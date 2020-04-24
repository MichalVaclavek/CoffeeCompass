
package cz.fungisoft.coffeecompass.domain.weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Class/object representing Sys part of the {@link WeatherData} returned by OpenWeather API<br>
 * at https://openweathermap.org/current
 * <p>
 * The class was created by http://www.jsonschema2pojo.org/ tools.
 * 
 * @author Michal Vaclavek
 *
 */
public class Sys {

    private Integer type;
    private Integer id;
    private String country;
    private Long sunrise;
    private Long sunset;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getSunrise() {
        return sunrise;
    }

    public void setSunrise(Long sunrise) {
        this.sunrise = sunrise;
    }

    public Long getSunset() {
        return sunset;
    }

    public void setSunset(Long sunset) {
        this.sunset = sunset;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
