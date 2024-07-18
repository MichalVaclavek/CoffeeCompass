package cz.fungisoft.users.security;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
