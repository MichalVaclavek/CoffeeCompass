package cz.fungisoft.coffeecompass.controller.rest;

import com.google.common.collect.ImmutableMap;
import cz.fungisoft.coffeecompass.controller.models.rest.TokenRefreshRequest;
import cz.fungisoft.coffeecompass.entity.RefreshToken;
import cz.fungisoft.coffeecompass.exceptions.rest.TokenRefreshException;
import cz.fungisoft.coffeecompass.listeners.OnRegistrationCompleteEvent;
import cz.fungisoft.coffeecompass.service.tokens.RefreshTokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.AuthRESTResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.rest.BadAuthorizationRESTRequestException;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.service.tokens.TokenService;
import cz.fungisoft.coffeecompass.service.user.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.user.UserService;

@Tag(name = "Users", description = "User management")
@RestController
@RequestMapping("/rest/public/user")
public class UsersControllerPublicREST {

    @NonNull
    private final CustomRESTUserAuthenticationService authentication;
      
    @NonNull
    private final UserService usersService;
    
    @NonNull
    private final TokenService tokenService;

    @NonNull
    RefreshTokenService refreshTokenService;
    
    private final MessageSource messages;
    
    private final ApplicationEventPublisher eventPublisher;
    
    
    public UsersControllerPublicREST(@NonNull
                                     @Qualifier("jwtTokenUserAuthenticationService")
                                     CustomRESTUserAuthenticationService authentication,
                                     @NonNull UserService usersService,
                                     TokenService tokenService,
                                     RefreshTokenService refreshTokenService,
                                     ApplicationEventPublisher eventPublisher,
                                     MessageSource messages) {
        super();
        this.authentication = authentication;
        this.usersService = usersService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.messages = messages;
        this.eventPublisher = eventPublisher;
    }


    @PostMapping("/register")
    public ResponseEntity<AuthRESTResponse> register(@Valid @RequestBody SignUpAndLoginRESTDto registerRequest, Locale locale) {
        
        Optional<User> existing = usersService.findByUserName(registerRequest.getUserName());
        if (existing.isPresent()) {
            throw new InvalidParameterValueException("User", "userName", registerRequest.getUserName(), messages.getMessage("error.user.name.used", null, locale));
        }
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()
            && !usersService.isEmailUnique(null, registerRequest.getEmail())) {
                throw new InvalidParameterValueException("User", "email", registerRequest.getEmail(), messages.getMessage("error.user.emailused", null, locale));
        }
        if (registerRequest.getPassword().isEmpty()) {
            throw new InvalidParameterValueException("User", "password", "", messages.getMessage("error.user.password.empty", null, locale));
        }
      
        // Saves user to DB
        User newUser = usersService.registerNewRESTUser(registerRequest);
        
        // Sent new user's e-mail address verification e-mail
        if (newUser != null && newUser.getEmail() != null && !newUser.getEmail().isEmpty()) {
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, locale)); // invokes sending verification e-mail to the new user
        }
        
        return login(registerRequest, locale);
    }
    
    
    @PostMapping("/login")
    public ResponseEntity<AuthRESTResponse> login(@Valid @RequestBody SignUpAndLoginRESTDto loginRequest, Locale locale) {
        
        String token = authentication.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getDeviceID())
                                     .orElseThrow(() -> new BadAuthorizationRESTRequestException(messages.getMessage("error.user.login.failed", null, locale)));
        
        long expiryDate = Long.parseLong(tokenService.verify(token).get("exp"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginRequest.getUserName());
        AuthRESTResponse authResponse = new AuthRESTResponse(token, expiryDate, refreshToken.getToken());
        
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<AuthRESTResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    Map<String, String> tokenAttributes = ImmutableMap.of("deviceID", request.getDeviceID(), "userName", user.getUserName());
                    String accessToken = tokenService.expiring(tokenAttributes);
                    long expiryDate = Long.parseLong(tokenService.verify(accessToken).get("exp"));
                    return ResponseEntity.ok(new AuthRESTResponse(accessToken, expiryDate, requestRefreshToken));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
    }
}
