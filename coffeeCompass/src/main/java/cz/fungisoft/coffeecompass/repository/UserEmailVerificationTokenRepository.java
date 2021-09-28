package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserEmailVerificationToken;

public interface UserEmailVerificationTokenRepository extends JpaRepository<UserEmailVerificationToken, Long> {

    UserEmailVerificationToken findByToken(String token);
    
    UserEmailVerificationToken findByUser(User user);
    
    void deleteByToken(String token);

    void deleteByUser(User user);
}
