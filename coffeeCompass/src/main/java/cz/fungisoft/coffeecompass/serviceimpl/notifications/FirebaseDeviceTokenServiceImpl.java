package cz.fungisoft.coffeecompass.serviceimpl.notifications;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.DeviceFirebaseToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.FirebaseDeviceTokenRepository;
import cz.fungisoft.coffeecompass.service.notifications.FirebaseDeviceTokenService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements {@link FirebaseDeviceTokenService} to handle creation and deletition
 * operations with {@link DeviceFirebaseToken} objects
 * 
 * @author Michal V.
 *
 */
@Service
@Transactional
@Slf4j
public class FirebaseDeviceTokenServiceImpl implements FirebaseDeviceTokenService {

    private final FirebaseDeviceTokenRepository firebaseDeviceTokenRepository;
    
    
    public FirebaseDeviceTokenServiceImpl(FirebaseDeviceTokenRepository firebaseDeviceTokenRepository) {
        super();
        this.firebaseDeviceTokenRepository = firebaseDeviceTokenRepository;
    }

    @Override
    public void saveToken(DeviceFirebaseToken token) {
        firebaseDeviceTokenRepository.save(token);
        log.debug("Token {} saved.", token.getFirebaseToken());
    }

    
    @Override
    public Optional<DeviceFirebaseToken> getOneByTokenString(String tokenString) {
        return firebaseDeviceTokenRepository.getOneToken(tokenString);
    }

    @Override
    public List<DeviceFirebaseToken> getTokensForUser(User user) {
        return firebaseDeviceTokenRepository.getAllTokensForUser(user.getLongId());
    }

    @Override
    public void deleteTokensOfUser(User user) {
        firebaseDeviceTokenRepository.deleteAllFromUser(user.getLongId());
        log.info("All user's Firebase tokens deleted. User name: {}", user.getUserName());
    }

    @Override
    public void deleteToken(String token) {
        firebaseDeviceTokenRepository.deleteToken(token);
        log.info("Firebase token deleted. Token: {}", token);
    }
}
