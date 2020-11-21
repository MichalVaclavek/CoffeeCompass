
package cz.fungisoft.coffeecompass.domain.weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Class/object representing Clouds part of the {@link WeatherData} returned by OpenWeather API<br>
 * at https://openweathermap.org/current
 * <p>
 * The class was created by http://www.jsonschema2pojo.org/ tools.
 * 
 * @author Michal Vaclavek
 *
 */
public class Clouds {

    private Integer all;
    private Map<String, Object> additionalProperties = new HashMap<>();

    public Integer getAll() {
        return all;
    }

    public void setAll(Integer all) {
        this.all = all;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
