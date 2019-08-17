package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;

public interface UserVerificationTokenRepository extends JpaRepository<UserVerificationToken, Long>
{
    public UserVerificationToken findByToken(String token);
    
    public UserVerificationToken findByUser(User user);
    
    public void deleteByToken(String token);
}
