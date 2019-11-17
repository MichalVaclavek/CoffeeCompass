package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.fungisoft.coffeecompass.entity.MobileUserLoginToken;
import cz.fungisoft.coffeecompass.entity.User;

public interface MobileUserLoginTokenRepository extends JpaRepository<MobileUserLoginToken, Long>
{
    public MobileUserLoginToken findByToken(String token);
    
    public MobileUserLoginToken findByUser(User user);
    
    public MobileUserLoginToken findByDeviceId(String deviceId);
    
    public MobileUserLoginToken findByTokenAndDeviceId(String token, String deviceId);
    
    public void deleteByToken(String token);
}
