package cz.fungisoft.coffeecompass.serviceimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.controller.models.AuthProviders;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.UserProfileTypeEnum;
import cz.fungisoft.coffeecompass.repository.PasswordResetTokenRepository;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UserVerificationTokenRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.security.oauth2.user.OAuth2UserInfo;
import cz.fungisoft.coffeecompass.service.UserSecurityService;
import cz.fungisoft.coffeecompass.service.UserService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;
 
/**
 * Implements all operations related to user data manipulation.<br>
 * Saves new users, updates user's profile, retrieves user's data, and so on.
 *    
 * @author Michal Vaclavek
 *
 */
@Service("userService")
@Transactional
@Log4j2
public class UserServiceImpl implements UserService
{
    private UsersRepository usersRepository;
   
    private PasswordEncoder passwordEncoder;
        
    private MapperFacade mapperFacade;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private UserVerificationTokenRepository registartionTokenRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private UserSecurityService userSecurityService;
    
     
    /**
     * Konstruktor, ktery vyuzije Spring pro injection zavislosti. Neni potreba uvadet anotaci @Autowired na atributech
     *         
     * @param usersRepository
     * @param passwordEncoder
     * @param mapperFacade
     */
    @Autowired
    public UserServiceImpl(UsersRepository usersRepository, PasswordEncoder passwordEncoder, MapperFacade mapperFacade) {
        super();
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapperFacade = mapperFacade;
    }
    
    // ** Findind User ** /

    @Override
    public UserDTO findByIdToTransfer(Long id) {        
        User user = findById(id);
        return addNonPersistentInfoToUser(user);
    }
    
    @Override
    public User findById(Long id) {
        User user = usersRepository.findById(id).orElse(null);
        
        if (user == null) {
            log.warn("User with id {} not found.", id);
        }
        else {
            log.info("User with id {} found.", id);
        }
        
        return user;
    }
    
    @Override
    public UserDTO findByUserNameToTransfer(String userName) {      
        Optional<User> user = findByUserName(userName);
        if (user.isPresent()) {
            return addNonPersistentInfoToUser(user.get());
        } else
            return null;
    }
    
    @Override
    public Optional<User> findByUserName(String userName) {
        Optional<User> user = usersRepository.searchByUsername(userName);
        
        if (!user.isPresent()) {
            log.warn("User with user name {} not found.", userName);
        }
        else {
            log.info("User with user name {} found.", userName);
        }
        
        return user;
    }
    
    /**
     * Pomocna metoda k doplneni aktualnich dat o User uctu, ktery se prenasi na klienta. 
     * 
     * @param userName
     * @return
     */
    private UserDTO addNonPersistentInfoToUser(User user) {
        UserDTO userDTO = null;
        
        if (user != null) {
            userDTO = mapperFacade.map(user, UserDTO.class);
            userDTO.setHasADMINRole(hasADMINRole(user));
            userDTO.setToManageItself(isLoggedInUserToManageItself(user));
        }
        
        return userDTO;
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        
        Optional<User> user = usersRepository.searchByEmail(email);
        
        if (!user.isPresent()) {
            log.warn("User with e-mail {} not found.", email);
        }
        else {
            log.info("User with e-mail {} found. User name: ", email, user.get().getUserName());
        }
        
        return user;
    }
 
    /** Saving and Updating **/
    
    /**
     * Used to save User profile registered via social login after first successful
     * login redirection URL (/oauth2/loginSuccess/?token=) processed.
     */
    @Override
    public User saveUser(User user) {
        log.info("Saving user name {}", user.getUserName());
        return usersRepository.save(user);
    }
 
    /**
     * Updates logged-in User data. If the userName is changed, Spring security context
     * must be changed too.<br>
     * <p>
     * Updates also User data of another user, if logged-in has ADMIN rights. In such case,
     * only password or Roles (except ADMIN) can be changed.<br>
     * If all ROLES should be removed, then USER role is inserted (User has to have at least one role)<br>
     * If ADMIN removes his ADMIN role, than it is applied also in Spring authentication object, immediately.
     */
    @Override
    public User updateUser(User user) {
        User entity = usersRepository.findById(user.getId()).orElse(null);
        
        if (entity != null) {
            String newUserName = entity.getUserName(); // current user name, can be changed and bocomes newUserName
            String newPasswd = entity.getPassword();
            
            if (user.getPassword() != null && !user.getPassword().isEmpty() ) {
                newPasswd = user.getPassword();
                entity.setPassword(passwordEncoder.encode(newPasswd));
            }
            
            // Can be empty, if ADMIN is editing another user
            if (user.getFirstName() != null)
                entity.setFirstName(user.getFirstName());
            if (user.getLastName() != null)
                entity.setLastName(user.getLastName());
            
            // Can be empty, e-mail is not mandatory
            if (user.getEmail() != null) {
                if (entity.getEmail().isEmpty()
                    || !entity.getEmail().equalsIgnoreCase(user.getEmail())) { // novy, neprazdny email => zatim nepotvrzeny
                    entity.setRegisterEmailConfirmed(false);
                } 
                entity.setEmail(user.getEmail());
            }
            
            // User profiles can be empty during update - means remove all roles - this is not applicable for ADMIN user
            // ADMIN cannot remove ADMIN role of another user
            
            // curent and new User's ROLES
            Set<UserProfile> newUserProfiles = entity.getUserProfiles();
            
            // New desired ROLES - can be empty
            Set<UserProfile> updatedUserProfiles = user.getUserProfiles(); 
            
            // Checks desired (updatedUserProfiles) ROLES against current (newUserProfiles) ROLES
            if (hasADMINRole(entity)) {
                newUserProfiles.clear();
                if (!isLoggedInUserToManageItself(entity))
                    newUserProfiles.add(userProfileRepository.searchByType("ADMIN"));
                newUserProfiles.addAll(updatedUserProfiles);
            } else {
                if (updatedUserProfiles != null)
                    newUserProfiles = updatedUserProfiles;
                if (newUserProfiles.isEmpty()) // There must be at least one basic user ROLE
                    newUserProfiles.add(userProfileRepository.searchByType("USER"));
            }
            
            // If user's e-mail is not confirmed, remove DBA role
            if (!entity.isRegisterEmailConfirmed()) {
                newUserProfiles.remove(userProfileRepository.searchByType("DBA"));
            }
            
            entity.setUserProfiles(newUserProfiles);
            
            // User name can be empty, if ADMIN is editing another user
            if (user.getUserName() != null && !user.getUserName().isEmpty()) {
                newUserName = user.getUserName();
            }
            
            // logged-in User has updated it's own data - 'Spring authentication object' has to be updated too.
            if (isLoggedInUserToManageItself(entity)) { 
                userSecurityService.updateCurrentAuthentication(entity, newUserName, newPasswd);
            }
            // User name must be updated (saved) after Spring authentication object update, otherwise isLoggedInUserToManageItself(entity) resolves that 
            // another user (ADMIN) is updating this 'user' as user name was already updated in DB.
            entity.setUserName(newUserName);
            entity.setUpdatedOn(new Timestamp(new Date().getTime()));
        }
        
        log.info("User name {} updated.", entity.getUserName());
        return entity;
    }
    
    @Override
    public User updateUser(UserDTO userDTO) {
        return updateUser(mapperFacade.map(userDTO,  User.class));
    }
    
    /**
     * Saves new user data to DB. Converts from UserDataDto to standard User
     */
    @Override
    public User save(UserDTO registration) {
        User user = new User();
        
        user.setUserName(registration.getUserName());
        user.setFirstName(registration.getFirstName());
        user.setLastName(registration.getLastName());
        user.setEmail(registration.getEmail());
        user.setRegisterEmailConfirmed(false);
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        
        user.setUpdatedSites(0);
        user.setCreatedSites(0);
        user.setDeletedSites(0);    
        
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        // Only basic USER role can be assigned to commom new user 
        userProfiles.add(userProfileRepository.searchByType("USER"));
        user.setUserProfiles(userProfiles);
        
        user.setEnabled(true);
        user.setAuthProvider(AuthProviders.local);
        user.setCreatedOn(new Timestamp(new Date().getTime()));
        
        log.info("Saving new user name {}", user.getUserName());
        return usersRepository.save(user);
    }
    
    /**
     * Special version of user save method to save User loged-in via OAuth2 authentication provider.
     */
    @Override
    public User saveOAuth2User(ClientRegistration clientRegistration, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        user.setAuthProvider(AuthProviders.valueOf(clientRegistration.getRegistrationId()));
        user.setUserName(oAuth2UserInfo.getName());
        user.setFirstName(oAuth2UserInfo.getFirstName());
        user.setLastName(oAuth2UserInfo.getLastName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setRegisterEmailConfirmed(oAuth2UserInfo.isEmailConfirmed());
        
        user.setUpdatedSites(0);
        user.setCreatedSites(0);
        user.setDeletedSites(0);
        
        // User is set to enabled after first login when the registration form is shown
        // to enable user registration directly on coffeecompass.cz within respective controller
        
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        // Only basic USER role can be assigned to commom new user 
        userProfiles.add(userProfileRepository.searchByType("USER"));
        // If user's e-mail is confirmed, add DBA role
        if (user.isRegisterEmailConfirmed()) {
            userProfiles.add(userProfileRepository.searchByType("DBA"));
        } 
        user.setUserProfiles(userProfiles);
//        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setCreatedOn(new Timestamp(new Date().getTime()));
        
        log.info("Saving new OAuth2 user name {}", user.getUserName());
        return usersRepository.save(user);
    }

    /**
     * Special version of user update method to update user's profile loged-in via OAuth2 authentication provider.
     */
    @Override
    public User updateOAuth2User(User existingUser, OAuth2UserInfo oAuth2UserInfo) {

        existingUser.setUserName(oAuth2UserInfo.getName());
        existingUser.setFirstName(oAuth2UserInfo.getFirstName());
        existingUser.setLastName(oAuth2UserInfo.getLastName());
        existingUser.setRegisterEmailConfirmed(oAuth2UserInfo.isEmailConfirmed());
//        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        
        Set<UserProfile> existingUserProfiles = existingUser.getUserProfiles();
        // If user's e-mail is not confirmed, remove DBA role
        if (!existingUser.isRegisterEmailConfirmed()) {
            existingUserProfiles.remove(userProfileRepository.searchByType("DBA"));
        } else { // user's e-mail confirmed, add DBA ROLE if it is not already assigned
            if (!hasDBARole(existingUser)) { // probably not needed as userProfiles is a Set<UserProfile> and one UserProfile cannot be twice in one Set
                existingUserProfiles.add(userProfileRepository.searchByType("DBA"));
            }
        }
        
        existingUser.setUpdatedOn(new Timestamp(new Date().getTime()));
        
        log.info("Updating OAuth2 user name {}", existingUser.getUserName());
        return usersRepository.save(existingUser);
    }
    
    
    @Override
    public void deleteUserBySSO(String ssoId) {
        usersRepository.deleteByUserName(ssoId);
        log.info("User name {} deleted.", ssoId);
    }
           
    @Override
    public void deleteUserById(Long id) {
        usersRepository.deleteById(id);
        log.info("User id {} deleted.", id);
    }
    
    @Override
    public void clearUserDataById(Long userId) {
        User userToClear = usersRepository.findById(userId).orElse(null);
        
        if (userToClear != null) {
            userToClear.setPassword("");
            
            userToClear.setFirstName("");
            userToClear.setLastName("");
            
            userToClear.setRegisterEmailConfirmed(false);
            userToClear.setEmail("");
            
            userToClear.setUpdatedOn(new Timestamp(new Date().getTime()));
            
            userToClear.setBanned(true);
            userToClear.setEnabled(false);
            
            log.info("User id {} cleared.", userId);
        } else {
            log.warn("User id {} to be cleared not found.", userId);
        }
    }
 
    @Override
    public List<UserDTO> findAllUsers() {
        List<UserDTO> usersDTO = mapperFacade.mapAsList(usersRepository.findAll(), UserDTO.class);
        
        for (UserDTO userDTO : usersDTO) {
            userDTO.setHasADMINRole(hasADMINRole(mapperFacade.map(userDTO,  User.class)));
            userDTO.setToManageItself(isLoggedInUserToManageItself(mapperFacade.map(userDTO,  User.class)));
        }
        log.info("All users retrieved: {}", usersDTO.size());
        return usersDTO;
    }
 
    @Override
    public boolean isUserNameUnique(Long id, String sso) {
        Optional<User> user = usersRepository.searchByUsername(sso);
        return (!user.isPresent() || ((id != null) && (user.get().getId() == id)));
    }

    /**
     * Verifies, if e-mail address is already used by another user.<br>
     * Empty e-mail returns true as it is allowed for every user.<br>
     * If verified emal belongs to the user of the verified id,<br>
     * then return true as the user verifies it's own e-mail and it is allowed.
     * 
     * @param id - id of the user whos's e-mail is to be verified
     * @param email - address to be verified if it is unique.
     */
    @Override
    public boolean isEmailUnique(Long id, String email) {
        Optional<User> user = usersRepository.searchByEmail(email);
        return (!user.isPresent() || ((id != null) && (user.get().getId() == id)));
    }
    
    @Override
    public boolean hasADMINRole(User user) {
        return user.getUserProfiles().stream().anyMatch(p -> p.getType().equals(UserProfileTypeEnum.ADMIN.getUserProfileType()));
    }
    
    @Override
    public boolean hasDBARole(User user) {
        return user.getUserProfiles().stream().anyMatch(p -> p.getType().equals(UserProfileTypeEnum.DBA.getUserProfileType()));
    }
    
    @Override
    public boolean hasADMINorDBARole(User user) {
        return hasADMINRole(user) || hasDBARole(user);
    }
    
    /**
     * Overeni, ze logged-in uzivatel je stejny jako {@code user}
     */
    @Override
    public boolean isLoggedInUserToManageItself(User user) {
        Optional<User> loggedInUser = getCurrentLoggedInUser();
        return loggedInUser.isPresent() && loggedInUser.get().getId() == user.getId();        
    }

    @Override
    public boolean isADMINloggedIn() {
        Optional<User> loggedInUser = getCurrentLoggedInUser();
        return loggedInUser.isPresent() && hasADMINRole(loggedInUser.get());
    }
    
    /** Methods for User's e-mail verification or password reset tokens **/
    
    @Override
    public void saveVerifiedRegisteredUser(User user, String token) {
        user.setRegisterEmailConfirmed(true);
        
        Set<UserProfile> currentUserProfiles = user.getUserProfiles();
        // E-mail confirmed, higher privileges can be added to User
        currentUserProfiles.add(userProfileRepository.searchByType("DBA"));
        user.setUserProfiles(currentUserProfiles);
        usersRepository.save(user);
    }

    @Override
    public User getUserByRegistrationToken(String verificationToken) {
        User user = registartionTokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

    /**
     * Changes user's password.
     * 
     * @param user / user whos's password is to be changed. cannot be null.
     * @param newPassword - newPassword of the user. cannot be null or empty
     * 
     * @return true if password changed successfully.
     */
    @Override
    public boolean changeUserPassword(User user, String newPassword) {
        if (user == null || newPassword == null || newPassword.isEmpty()) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return usersRepository.save(user) != null;
    }

    @Override
    public User getUserByPasswordResetToken(String pswdResetToken) {
        User user = passwordResetTokenRepository.findByToken(pswdResetToken).getUser();
        return user;
    }
    
    @Override
    public Optional<User> getCurrentLoggedInUser() {
        return this.usersRepository.searchByUsername(userSecurityService.getCurrentLoggedInUserName());
    }

    @Override
    public Optional<UserDTO> getCurrentLoggedInUserDTO() {
        return Optional.ofNullable(findByUserNameToTransfer(userSecurityService.getCurrentLoggedInUserName()));
    }

}