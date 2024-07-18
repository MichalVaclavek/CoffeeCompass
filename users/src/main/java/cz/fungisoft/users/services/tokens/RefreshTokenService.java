package cz.fungisoft.users.services.tokens;

import cz.fungisoft.users.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {


    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(String userName);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByUserId(Long userId);
}
