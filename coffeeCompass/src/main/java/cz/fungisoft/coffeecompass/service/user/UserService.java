package cz.fungisoft.coffeecompass.service.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.security.oauth2.user.OAuth2UserInfo;

 
public interface UserService {

    Optional<UserDTO> findByExtIdToTransfer(UUID id);
    Optional<UserDTO> findByExtIdToTransfer(String id);
    Optional<UserDTO> findByUserNameToTransfer(String userName);
    
    @PreAuthorize("hasRole('ADMIN')")
    Optional<User> findByExtId(UUID id);
    @PreAuthorize("hasRole('ADMIN')")
    Optional<User> findByExtId(String extId);
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
     
    User saveUser(User user);
    // Pro ulozeni nove vytvoreneho usera z DTO objektu ve formulari
    User save(UserDTO registration);
    
    /**
     *  Pro ulozeni nove vytvoreneho usera z POST requestu pres REST
     *  rozhrani tj. prevazne z mob. app.
     *  
     * @param restRegisterDTO
     * @return
     */
    User registerNewRESTUser(SignUpAndLoginRESTDto restRegisterDTO);
    
    /**
     *  Pro ulozeni updatovaneho Usera poslaneho clientem pomoci PUT requestu pres REST
     *  rozhrani tj. prevazne z mob. app.
     *  
     * @param restUpdateUserDTO - user object to be updated
     * @return updated UserDTO data
     */
    UserDTO updateRESTUser(SignUpAndLoginRESTDto restUpdateUserDTO);
    
    /**
     * Saves new OAuth2Info user data obtained from OAuth2 provider
     * @return
     */
    User saveOAuth2User(ClientRegistration clientRegistration, OAuth2UserInfo oAuth2UserInfo);
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    User updateUser(UserDTO user);

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    User updateUser(User user);
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    User updateOAuth2User(User existingUser, OAuth2UserInfo oAuth2UserInfo); 
    
    @PreAuthorize("hasRole('ADMIN')")
    void deleteUserBySSO(String sso);
    
    @PreAuthorize("hasRole('ADMIN')")
    void deleteUserById(UUID id);

    @PreAuthorize("hasRole('ADMIN')")
    void deleteUserById(String id);
 
    @PreAuthorize("hasRole('ADMIN')")
    List<UserDTO> findAllUsers();
    
    boolean isUserNameUnique(UUID id, String sso);
    boolean isEmailUnique(UUID id, String email);
    
    boolean hasADMINRole(User user);
    boolean hasADMINorDBARole(User user);
    boolean hasDBARole(User user);
    
    /**
     * A method to return true, if the logged-in subscriber is same
     * as User
     * 
     * @param user - a User account to evaluate/compare against logged-in User
     * @return true, if the logged-in subscriber is same as "user"
     */
    boolean isLoggedInUserToManageItself(User user);
    
    /**
     * @return true if currently logged-in user has ADMIN role
     */
    boolean isADMINloggedIn();
    
    Optional<User> getCurrentLoggedInUser();
    Optional<UserDTO> getCurrentLoggedInUserDTO();

    /**
     * Saves/updated User, which was verified by token.
     * 
     * @param user - user, who was verified by token
     * @param token - verification token used to verify user
     */
    void saveVerifiedRegisteredUser(User user, String token);
    
    User getUserByRegistrationToken(String verificationToken);

    boolean changeUserPassword(User user, String newPassword);
    
    User getUserByPasswordResetToken(String pswdResetToken);

    /**
     * Clears all data related to the given user id, except username.<br>
     * Sets 'enabled' to false and 'banned' to true.
     * 
     * @param userId
     */
    void clearUserDataById(UUID userId);
}
