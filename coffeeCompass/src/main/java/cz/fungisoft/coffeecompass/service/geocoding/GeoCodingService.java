package cz.fungisoft.coffeecompass.service.geocoding;

import com.google.maps.PendingResult;
import com.google.maps.model.GeocodingResult;


/**
 * Service interface to declare methods needed for resoling place from geo coordinates.
 * <p>
 * Wrapper for expected implementation based on  https://github.com/googlemaps/google-maps-services-java
 * 
 * @author Michal Vaclavek
 *
 */
public interface GeoCodingService {
    
    /**
     * Gets GeocodingResult geo coordinates, sync.
     * 
     * @param lat - geo latitude
     * @param lon - geo longitude
     * @return
     */
    GeocodingResult[] getReverseGeocodingResult(double lat, double lon);
    
    /**
     * Performs async. call for reverese Geocoding, result processed in callBack method like:
     * 
     * 
     *  final List<GeocodingResult[]> resps = new ArrayList<>();
     *  PendingResult.Callback<GeocodingResult[]> callback =
          new PendingResult.Callback<GeocodingResult[]>() {
            @Override
            public void onResult(GeocodingResult[] result) {
              resps.add(result);
            }

            @Override
            public void onFailure(Throwable e) {
              fail("Got error when expected success.");
            }
          };
     * 
     * 
     * @param lat - geo latitude
     * @param lon - geo longitude
     * @return
     */
    void performReverseGeocodingResult(double lat, double lon, PendingResult.Callback<GeocodingResult[]> callBack);
    
    /**
     * Gets name of town for given geo coordinations
     * 
     * @return name of town under given coordinates
     */
    String getTownName(double lat, double lon);
    
    /**
     * Used to obtain info about town/place
     * 
     * @param town
     * @return
     */
    GeocodingResult[] getTownInfo(String town);
    
    /**
     * Validates name of town
     * 
     * @param town
     * @return true if name of town is found in geoCoding API or another service 
     */
    boolean validateTownName(String town);
}
