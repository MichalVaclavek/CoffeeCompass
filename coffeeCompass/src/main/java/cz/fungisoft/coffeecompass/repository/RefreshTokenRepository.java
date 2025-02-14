package cz.fungisoft.coffeecompass.repository;

import cz.fungisoft.coffeecompass.entity.RefreshToken;
import cz.fungisoft.coffeecompass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
//    @Override
//    Optional<RefreshToken> findById(Long id);
    RefreshToken findByUser(User user);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
