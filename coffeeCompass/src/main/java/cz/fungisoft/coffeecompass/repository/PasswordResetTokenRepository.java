package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>
{
    public PasswordResetToken findByToken(String token);
    
    public PasswordResetToken findByUser(User user);
    
    public void deleteByToken(String token);
}
