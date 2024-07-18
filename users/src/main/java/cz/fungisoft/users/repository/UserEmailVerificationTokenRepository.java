package cz.fungisoft.users.repository;

import cz.fungisoft.users.entity.User;
import cz.fungisoft.users.entity.UserEmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEmailVerificationTokenRepository extends JpaRepository<UserEmailVerificationToken, Long> {

    UserEmailVerificationToken findByToken(String token);
    
    UserEmailVerificationToken findByUser(User user);
    
    void deleteByToken(String token);

    void deleteByUser(User user);
}
