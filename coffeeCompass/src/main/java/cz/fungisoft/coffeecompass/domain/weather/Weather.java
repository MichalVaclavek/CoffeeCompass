
package cz.fungisoft.coffeecompass.domain.weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Class/object representing Weather part of the {@link WeatherData} returned by OpenWeather API<br>
 * at https://openweathermap.org/current
 * <p>
 * The class was created by http://www.jsonschema2pojo.org/ tools.
 * 
 * @author Michal Vaclavek
 *
 */
public class Weather {

    private Integer id;
    private String main;
    private String description;
    private String icon;
    
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
