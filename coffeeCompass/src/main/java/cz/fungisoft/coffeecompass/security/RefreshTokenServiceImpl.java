package cz.fungisoft.coffeecompass.security;

import cz.fungisoft.coffeecompass.entity.RefreshToken;
import cz.fungisoft.coffeecompass.exceptions.UserNotFoundException;
import cz.fungisoft.coffeecompass.exceptions.rest.TokenRefreshException;
import cz.fungisoft.coffeecompass.repository.RefreshTokenRepository;
import cz.fungisoft.coffeecompass.service.tokens.RefreshTokenService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwtauth.jwtRefreshtokenexpirationsec}")
    private Long refreshTokenDurationSecs;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserService userService;


    public RefreshTokenServiceImpl(UserService userService, RefreshTokenRepository refreshTokenRepository) {
        this.userService = userService;
        this.refreshTokenRepository = refreshTokenRepository;

    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(String userName) {
        return userService.findByUserName(userName).map(user -> {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(user);
            refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationSecs));
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken = refreshTokenRepository.save(refreshToken);
            return refreshToken;
        }).orElseThrow(() -> new UserNotFoundException("Wrong user id provided."));

    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        userService.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }
}
