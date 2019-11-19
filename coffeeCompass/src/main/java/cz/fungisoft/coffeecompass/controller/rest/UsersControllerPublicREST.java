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

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exception.BadRequestException;
import cz.fungisoft.coffeecompass.service.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.UserService;


@RestController
@RequestMapping("/rest/public/user")
public class UsersControllerPublicREST
{
    @NonNull
    private CustomRESTUserAuthenticationService authentication;
      
    @NonNull
    private UserService usersService;
    
    
    public UsersControllerPublicREST(@NonNull
                                     @Qualifier("jwtTokenUserAuthenticationService")
                                     CustomRESTUserAuthenticationService authentication,
                                     @NonNull UserService usersService) {
        super();
        this.authentication = authentication;
        this.usersService = usersService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignUpAndLoginRESTDto loginRequest) {
        
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
    public ResponseEntity<?> login(@Valid @RequestBody SignUpAndLoginRESTDto loginRequest) {
        
        String token = authentication.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getDeviceID())
                                     .orElseThrow(() -> new BadRequestException("invalid login and/or password"));
      
        return ResponseEntity.ok(token);
    }

}
