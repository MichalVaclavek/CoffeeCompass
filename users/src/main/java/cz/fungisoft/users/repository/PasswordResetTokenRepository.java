package cz.fungisoft.users.repository;

import cz.fungisoft.users.entity.PasswordResetToken;
import cz.fungisoft.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
