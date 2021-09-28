package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Repository interface for saving/reading {@link PasswordResetToken} objects.
 * 
 * @author Michal Vaclavek
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);
    
    PasswordResetToken findByUser(User user);
    
    void deleteByToken(String token);
}
