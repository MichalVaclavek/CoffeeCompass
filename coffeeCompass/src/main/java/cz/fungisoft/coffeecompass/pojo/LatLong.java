package cz.fungisoft.coffeecompass.pojo;

import lombok.Data;

/**
 * Pomocna trida to hold latutide and longitude values.
 * Used especialy for counting average values of CofeeSites lists in case of
 * more coffeeSite are to be displayed in one map together and with "search from point"
 * 
 * @author Michal Vavlavek
 *
 */
@Data
public class LatLong
{
    private double latitude = 0.0d;
    private double longitude = 0.0d;
    
    public LatLong(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    
}
