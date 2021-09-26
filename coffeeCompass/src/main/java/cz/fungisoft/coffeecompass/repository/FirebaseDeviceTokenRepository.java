package cz.fungisoft.coffeecompass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.DeviceFirebaseToken;


/**
 * Interface pro ukladani/cteni objektu typu DeviceFirebaseToken do DB.
 * 
 * @author Michal Vaclavek
 */
public interface FirebaseDeviceTokenRepository extends JpaRepository<DeviceFirebaseToken, Integer> {
    
    /**
     * Get all Tokens for given user
     * 
     * @param userId
     * @return
     */
    @Query("SELECT dft FROM DeviceFirebaseToken dft WHERE user.id=?1")
    List<DeviceFirebaseToken> getAllTokensForUser(Long userId);
    
    /**
     * Get one DeviceFirebaseToken according token string
     * 
     * @param tokenString
     * @return
     */
    @Query("SELECT dft FROM DeviceFirebaseToken dft WHERE firebaseToken=?1")
    Optional<DeviceFirebaseToken> getOneToken(String tokenString);
    
    @Modifying // required by Hibernate, otherwise there is an exception ' ... Illegal state ...'
    @Query("delete FROM DeviceFirebaseToken dft where user.id=?1")
    void deleteAllFromUser(Long userID);
    
    @Query("delete FROM DeviceFirebaseToken dft where firebaseToken=?1")
    void deleteToken(String tokenString);
}
