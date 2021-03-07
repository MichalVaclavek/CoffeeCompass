package cz.fungisoft.coffeecompass.service.user;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;


/**
 * Service to perform basic authentication/login and logout tasks
 * for REST (mobile app.) user.
 * 
 * @author Michal Vaclavek
 *
 */
public interface CustomRESTUserAuthenticationService {
    /**
     * Logs in with the given {@code username} and {@code password}.
     *
     * @param username
     * @param password
     * @return an {@link Optional} of a user when login succeeds
     */
    public Optional<String> login(String userName, String password, String deviceID);

    /**
     * Finds a user by its token.
     *
     * @param token for mobile user.
     * @return
     */
    public Optional<UserDetails> findByToken(String token);
    
    /**
     * Logs out the given input {@code user}.
     *
     * @param user the user to logout
     */
    public void logout(UserDetails user);
}
