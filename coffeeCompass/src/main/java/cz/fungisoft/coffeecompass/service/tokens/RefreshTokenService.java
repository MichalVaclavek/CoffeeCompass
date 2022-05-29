package cz.fungisoft.coffeecompass.service.tokens;

import cz.fungisoft.coffeecompass.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {


    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(String userName);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByUserId(Long userId);
}
