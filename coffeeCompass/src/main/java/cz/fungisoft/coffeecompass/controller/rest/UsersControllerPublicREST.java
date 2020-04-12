package cz.fungisoft.coffeecompass.controller.rest;

import lombok.NonNull;

import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.AuthRESTResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.rest.BadAuthorizationRESTRequestException;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.listeners.OnRegistrationCompleteEvent;
import cz.fungisoft.coffeecompass.service.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.TokenService;
import cz.fungisoft.coffeecompass.service.UserService;
import io.swagger.annotations.Api;

@Api // Anotace Swagger
@RestController
@RequestMapping("/rest/public/user")
public class UsersControllerPublicREST
{
    @NonNull
    private CustomRESTUserAuthenticationService authentication;
      
    @NonNull
    private UserService usersService;
    
    @NonNull
    private TokenService tokens;
    
    private MessageSource messages;
    
    private TokenCreateAndSendEmailService verificationTokenSendEmailService;
    
    
    public UsersControllerPublicREST(@NonNull
                                     @Qualifier("jwtTokenUserAuthenticationService")
                                     CustomRESTUserAuthenticationService authentication,
                                     TokenCreateAndSendEmailService verificationTokenSendEmailService,
                                     @NonNull UserService usersService,
                                     TokenService tokens,
                                     MessageSource messages) {
        super();
        this.authentication = authentication;
        this.usersService = usersService;
        this.tokens = tokens;
        this.messages = messages;
        this.verificationTokenSendEmailService = verificationTokenSendEmailService;
    }


    @PostMapping("/register")
    public ResponseEntity<AuthRESTResponse> register(@Valid @RequestBody SignUpAndLoginRESTDto registerRequest, Locale locale) {
        
        Optional<User> existing = usersService.findByUserName(registerRequest.getUserName());
        if (existing.isPresent()) {
            throw new InvalidParameterValueException("User", "userName", registerRequest.getUserName(), messages.getMessage("error.user.name.used", null, locale));
        }
        if (registerRequest.getEmail() != null && !registerRequest.getEmail().isEmpty()) {
            if (!usersService.isEmailUnique(null, registerRequest.getEmail())) {
                throw new InvalidParameterValueException("User", "email", registerRequest.getEmail(), messages.getMessage("error.user.emailused", null, locale));
            }
        }
        if (registerRequest.getPassword().isEmpty()) {
            throw new InvalidParameterValueException("User", "password", "", messages.getMessage("error.user.password.empty", null, locale));
        }
      
        // Remove blank characters at the end of user name and e-mail
        User newUser = usersService.registerNewRESTUser(registerRequest);
        
        // Sent new user's e-mail address verification e-mail
        if (newUser != null && newUser.getEmail() != null && !newUser.getEmail().isEmpty()) {
            verificationTokenSendEmailService.setUserVerificationData(newUser, locale);
            verificationTokenSendEmailService.createAndSendVerificationTokenEmail();
        }
        
        return login(registerRequest, locale);
    }
    
    
    @PostMapping("/login")
    public ResponseEntity<AuthRESTResponse> login(@Valid @RequestBody SignUpAndLoginRESTDto loginRequest, Locale locale) {
        
        String token = authentication.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getDeviceID())
                                     .orElseThrow(() -> new BadAuthorizationRESTRequestException(messages.getMessage("error.user.login.failed", null, locale)));
        
        long expiryDate = Long.parseLong(tokens.verify(token).get("exp"));
        AuthRESTResponse authResponse = new AuthRESTResponse(token, expiryDate);
        
        return ResponseEntity.ok(authResponse);
    }

}
