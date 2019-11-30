package cz.fungisoft.coffeecompass.controller.rest;

import lombok.NonNull;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.AuthRESTResponse;
import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exception.BadRequestException;
import cz.fungisoft.coffeecompass.service.CustomRESTUserAuthenticationService;
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
    
    
    public UsersControllerPublicREST(@NonNull
                                     @Qualifier("jwtTokenUserAuthenticationService")
                                     CustomRESTUserAuthenticationService authentication,
                                     @NonNull UserService usersService,
                                     TokenService tokens) {
        super();
        this.authentication = authentication;
        this.usersService = usersService;
        this.tokens = tokens;
    }


    @PostMapping("/register")
    public ResponseEntity<AuthRESTResponse> register(@Valid @RequestBody SignUpAndLoginRESTDto loginRequest) {
        
        Optional<User> existing = usersService.findByUserName(loginRequest.getUserName());
        if (existing.isPresent()) {
            throw new BadRequestException("Name already in use.");
        }
        
        if (!usersService.isEmailUnique(null, loginRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }
      
        usersService.registerNewRESTUser(loginRequest);
            
        return login(loginRequest);
    }
    
    
    @PostMapping("/login")
    public ResponseEntity<AuthRESTResponse> login(@Valid @RequestBody SignUpAndLoginRESTDto loginRequest) {
        
        String token = authentication.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getDeviceID())
                                     .orElseThrow(() -> new BadRequestException("invalid login and/or password"));
        
        long expiryDate = Long.parseLong(tokens.verify(token).get("exp"));
        AuthRESTResponse authResponse = new AuthRESTResponse(token, expiryDate);
        
        return ResponseEntity.ok(authResponse);
    }

}
