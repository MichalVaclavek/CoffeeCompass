
package cz.fungisoft.coffeecompass.domain.weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Class/object representing Coords part of the {@link WeatherData} returned by OpenWeather API<br>
 * at https://openweathermap.org/current
 * <p>
 * The class was created by http://www.jsonschema2pojo.org/ tools.
 * 
 * @author Michal Vaclavek
 *
 */
public class Coord {

    private Double lon;
    private Double lat;
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
