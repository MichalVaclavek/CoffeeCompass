package cz.fungisoft.coffeecompass.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;

import cz.fungisoft.coffeecompass.security.UserPrincipal;

/**
 * Service to perform basic authentication/login and logout tasks
 * for REST (mobile app.) user.
 * 
 * @author Michal Vaclavek
 *
 */
public interface CustomRESTUserAuthenticationService
{
    /**
     * Logs in with the given {@code username} and {@code password}.
     *
     * @param username
     * @param password
     * @return an {@link Optional} of a user when login succeeds
     */
    public Optional<String> login(String username, String password, String deviceID);

    /**
     * Finds a user by its token.
     *
     * @param token for mobile user.
     * @return
     */
    public Optional<UserDetails> findByToken(String token);
    
    /**
     * Finds a user by device id
     *
     * @param device id of the user's mobile equipment
     * @return
     */
    //public Optional<UserDetails> findByDeviceId(String deviceId);
    
    /**
     * Finds a user by device id and token.
     *
     * @param device id of the user's mobile equipment
     * @return
     */
    //public Optional<UserDetails> findByTokenAndDeviceId(String token, String deviceId);

    /**
     * Logs out the given input {@code user}.
     *
     * @param user the user to logout
     */
    public void logout(UserDetails user);
}
