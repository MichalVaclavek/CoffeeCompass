package cz.fungisoft.coffeecompass.serviceimpl.geocoding;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PendingResult.Callback;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressType;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.LocationType;

import cz.fungisoft.coffeecompass.service.geocoding.GeoCodingService;
import lombok.extern.log4j.Log4j2;

/**
 * Service using googles's Geocoding API to retrive information about towns
 * under given geo coordinates. Also used to validate entered town/place name.
 * 
 * @author Michal Vaclavek
 */
@Service
@Log4j2
public class GeoCodingServiceImpl implements GeoCodingService {

    private static GeoApiContext geoApiContext;
    
    private final String GOOGLE_MAPS_API_KEY;
    
    private static final String API_CALL_ERROR = "GeocodingApi error: {}";
    
    
    public GeoCodingServiceImpl(@Value("${app.google.maps.api.key}") String apiKey) {
        GOOGLE_MAPS_API_KEY = apiKey;
    }
    
    @PostConstruct
    private void initialize() {
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(GOOGLE_MAPS_API_KEY)
                .build();
    }
    
    @PreDestroy
    public void preDestroy() {
        geoApiContext.shutdown();
    }
    
    /**
     * Returns complete GeocodingResult reverse geocoding result based on latitude and longitude
     */
    @Override
    public GeocodingResult[] getReverseGeocodingResult(double lat, double lon) {
        GeocodingResult[] results = null;
        LatLng latlng = new LatLng(lat, lon);
        try {
            results = GeocodingApi.newRequest(geoApiContext)
                                  .latlng(latlng)
                                  .await();
        } catch (InterruptedException e) {
            log.error(API_CALL_ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ApiException | IOException e) {
            log.error(API_CALL_ERROR, e.getMessage());
        }
        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //log.info(gson.toJson(results[0].addressComponents));
        return results;
    }

    /**
     * Using callBack was not working fine, when callback performs another thread operation.
     */
    @Override
    public void performReverseGeocodingResult(double lat, double lon, Callback<GeocodingResult[]> callBack) {
        LatLng latlng = new LatLng(lat, lon);
        GeocodingApi.newRequest(geoApiContext)
                    .latlng(latlng)
                    .setCallback(callBack);
    }

    /**
     * Returns name of the town on given coordinates using google API reverse geocoding
     */    
    @Override
    public String getTownName(double lat, double lon) {
        LatLng latlng = new LatLng(lat, lon);
        GeocodingResult[] geoCodingResults = null;
        String result = "";
        
        try {
            geoCodingResults = GeocodingApi.newRequest(geoApiContext)
                .latlng(latlng)
                .language("cs")
                //.locationType(LocationType.ROOFTOP)
                .resultType(AddressType.LOCALITY)
                .await();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (geoCodingResults.length > 0) {
                log.debug(gson.toJson(geoCodingResults[0].addressComponents));
                result = geoCodingResults[0].addressComponents[0].longName;
            }
        } catch (InterruptedException e) {
            log.error(API_CALL_ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ApiException | IOException e) {
            log.error(API_CALL_ERROR, e.getMessage());
        }
        return result;
    }

    /**
     * Retrieves GeocodingResult info about place/town entered.<br>
     * 
     * @param town - name of place, about which we need to obtain some info or validate
     */
    @Override
    public GeocodingResult[] getTownInfo(String town) {
        GeocodingResult[] results = null;
        try {
            results = GeocodingApi.newRequest(geoApiContext)
                                  .address(town)
                                  .language("cs")
                                  .region("CZ")
                                  .await();
        } catch (InterruptedException e) {
            log.error(API_CALL_ERROR, e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ApiException | IOException e) {
            log.error(API_CALL_ERROR, e.getMessage());
        }
        return results;
    }
    
    /**
     * Retrieves GeocodingResult info about place/town entered.<br>
     * and checks if the geoCodingResults[0].addressComponents[0].longName is equal to input town name
     * 
     * @param town - name of place/town, about which we need to check if it is valid town name
     */
    @Override
    public boolean validateTownName(String town) {
        boolean result = false;
        if (!town.isEmpty()) {
            GeocodingResult[] geoCodingResults = null;
            try {
                geoCodingResults = GeocodingApi.newRequest(geoApiContext)
                                      .address(town)
                                      .language("cs")
                                      .region("CZ") // for Czech Rep. usage only, generally do not need to specify
                                      .await();
                if (geoCodingResults != null && geoCodingResults.length > 0) {
                    result = town.equalsIgnoreCase(geoCodingResults[0].addressComponents[0].longName);
                }
            } catch (InterruptedException e) {
                log.error(API_CALL_ERROR, e.getMessage());
                Thread.currentThread().interrupt();
            } catch (ApiException | IOException e) {
                log.error(API_CALL_ERROR, e.getMessage());
            }
        }
        return result;
    }
}
