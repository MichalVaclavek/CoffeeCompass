package cz.fungisoft.users.services.user;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;


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
     * @param userName
     * @param password
     * @return an {@link Optional} of a user when login succeeds
     */
    Optional<String> login(String userName, String password, String deviceID);

    /**
     * Finds a user by its token.
     *
     * @param token for mobile user.
     * @return
     */
    Optional<UserDetails> findByToken(String token);
    
    /**
     * Logs out the given input {@code user}.
     *
     * @param user the user to logout
     */
    void logout(UserDetails user);
}
