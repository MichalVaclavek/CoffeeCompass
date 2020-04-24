
package cz.fungisoft.coffeecompass.domain.weather;

import java.util.HashMap;
import java.util.Map;

/**
 * Class/object representing Main part of the {@link WeatherData} returned by OpenWeather API<br>
 * at https://openweathermap.org/current
 * <p>
 * The class was created by http://www.jsonschema2pojo.org/ tools.
 * 
 * @author Michal Vaclavek
 *
 */
public class Main {

    private Double temp;
    private Double feels_like;
    private Integer temp_min;
    private Double temp_max;
    private Integer pressure;
    private Integer humidity;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        
        this.temp = Math.round(temp * 10.0) / 10.0;
    }

    public Double getFeels_like() {
        return feels_like;
    }

    public void setFeels_like(Double feelsLike) {
        this.feels_like = feelsLike;
    }

    public Integer getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(Integer tempMin) {
        this.temp_min = tempMin;
    }

    public Double getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(Double tempMax) {
        this.temp_max = tempMax;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
