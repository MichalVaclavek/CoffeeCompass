package cz.fungisoft.coffeecompass.service.notifications;

import java.util.List;
import java.util.Optional;

import cz.fungisoft.coffeecompass.entity.DeviceFirebaseToken;
import cz.fungisoft.coffeecompass.entity.User;



/**
 * Service to save/delete and get FirebaseDeviceTokens objects
 * 
 * @author Michal V.
 *
 */
public interface FirebaseDeviceTokenService {
    
    void saveToken(DeviceFirebaseToken token);
    
    List<DeviceFirebaseToken> getTokensForUser(User user);
    
    Optional<DeviceFirebaseToken> getOneByTokenString(String tokenString);
    
    void deleteTokensOfUser(User user);
    
    void deleteToken(String token);
}
