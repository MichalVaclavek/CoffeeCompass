package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserEmailVerificationToken;

public interface UserEmailVerificationTokenRepository extends JpaRepository<UserEmailVerificationToken, Long>
{
    public UserEmailVerificationToken findByToken(String token);
    
    public UserEmailVerificationToken findByUser(User user);
    
    public void deleteByToken(String token);
}
