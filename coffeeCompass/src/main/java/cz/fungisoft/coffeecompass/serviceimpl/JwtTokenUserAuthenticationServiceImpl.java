package cz.fungisoft.coffeecompass.serviceimpl;


import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.security.UserPrincipal;
import cz.fungisoft.coffeecompass.service.CustomRESTUserAuthenticationService;
import cz.fungisoft.coffeecompass.service.TokenService;
import cz.fungisoft.coffeecompass.service.UserSecurityService;
import cz.fungisoft.coffeecompass.service.UserService;

/**
 * This class is responsible of performing logging in and out operations for the users,<br>
 * as well as deliver the authentication tokens.<br>
 * @see https://octoperf.com/blog/2018/03/08/securing-rest-api-spring-security/
 * 
 * @author Michal Vaclavek
 *
 */
@Service("jwtTokenUserAuthenticationService")
public class JwtTokenUserAuthenticationServiceImpl implements CustomRESTUserAuthenticationService
{
    private UserService usersService;
    
    private TokenService tokens;
    
    private PasswordEncoder passwordEncoder;
    
    private UserSecurityService userSecurityService;
    
    
    public JwtTokenUserAuthenticationServiceImpl(UserService usersService,
                                                 TokenService tokens,
                                                 PasswordEncoder passwordEncoder,
                                                 UserSecurityService userSecurityService) {
        super();
        this.usersService = usersService;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.userSecurityService = userSecurityService;
    }

    /**
     * Login using username and password. Returns token.<br>
     * 'userName' can be email address too
     */
    @Override
    public Optional<String> login(final String userName, final String password, final String deviceID) {
        
        Optional<User> user = usersService.findByUserName(userName);
        
        if (!user.isPresent()) {
            user = usersService.findByEmail(userName);
        }
        
        Map<String, String> tokenAttributes = ImmutableMap.of("deviceID", deviceID, "userName", userName);
        
        return user.filter(u -> passwordEncoder.matches(password, u.getPassword()))
                   .map(u -> tokens.expiring(tokenAttributes));
    }

    /**
     * Login using token.
     */
    @Override
    @Transactional
    public Optional<UserDetails> findByToken(final String token) {
        
        return Optional.of(tokens.verify(token))
                .map(map -> map.get("userName"))
                .map(nameOrEmail -> usersService.findByUserName(nameOrEmail).map(Optional::of).orElseGet(() -> usersService.findByEmail(nameOrEmail))) // https://stackoverflow.com/questions/24599996/get-value-from-one-optional-or-another 
                .map(user -> UserPrincipal.create(user.get()));
    }

    @Override
    public void logout(final UserDetails user) {
        userSecurityService.logout(user);
    }

}
