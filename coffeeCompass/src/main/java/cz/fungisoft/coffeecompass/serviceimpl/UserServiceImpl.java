package cz.fungisoft.coffeecompass.serviceimpl;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.entity.UserProfileTypeEnum;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;
import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.repository.UserVerificationTokenRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.security.CustomUserDetailsService;
import cz.fungisoft.coffeecompass.security.IAuthenticationFacade;
import cz.fungisoft.coffeecompass.service.UserService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;
 
  
@Service("userService")
@Transactional
@Log4j2
public class UserServiceImpl implements UserService
{
    private UsersRepository usersRepository;
   
    private PasswordEncoder passwordEncoder;
        
    private MapperFacade mapperFacade;
        
    @Autowired
    private IAuthenticationFacade authenticationFacade;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private UserVerificationTokenRepository tokenRepository;
    
    /**
     * To get right authorities object as required by Spring security in case of ROLES modification.
     */
    @Autowired
    private CustomUserDetailsService userDetailsService;
     
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
    public UserDataDTO findByIdToTransfer(Integer id) {        
        User user = findById(id);
        
        if (user == null) {
            log.warn("User with id {} not found.", id);
        }
        else {
            log.warn("User with id {} found.", id);
        }
        
        return addNonPersistentInfoToUser(user);
    }
    
    @Override
    public User findById(Integer id) {
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
    public UserDataDTO findByUserNameToTransfer(String userName) {      
        User user = findByUserName(userName);
        
        if (user == null) {
            log.warn("User with user name {} not found.", userName);
        }
        else {
            log.info("User with user name {} found.", userName);
        }
        
        return addNonPersistentInfoToUser(user);
    }
    
    /**
     * Pomocna metoda k doplneni aktualnich dat o User uctu, ktery se prenasi na klienta. 
     * 
     * @param userName
     * @return
     */
    private UserDataDTO addNonPersistentInfoToUser(User user) {
        UserDataDTO userDTO = null;
        
        if (user != null) {
            userDTO = mapperFacade.map(user, UserDataDTO.class);
            userDTO.setHasADMINRole(hasADMINRole(user));
            userDTO.setToManageItself(isLoggedInUserToManageItself(user));
        }
        
        return userDTO;
    }
    
    @Override
    public User findByUserName(String userName) {
        User user = usersRepository.searchByUsername(userName);
        
        if (user == null) {
            log.warn("User with user name {} not found.", userName);
        }
        else {
            log.info("User with user name {} found.", userName);
        }
        
        return user;
    }
    
    @Override
    public User findByEmail(String email) {
        User user = null;
        if (!email.isEmpty())
            user = usersRepository.searchByEmail(email);
        
        if (user == null) {
            log.warn("User with e-mail {} not found.", email);
        }
        else {
            log.info("User with e-mail {} found. User name: ", email, user.getUserName());
        }
        
        return user ;
    }
 
    /** Saving and Updating **/
    
    @Override
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Saving user name {}", user.getUserName());
        return usersRepository.save(user);
    }
 
    /**
     * Updates logged-in User data. If the userName is changed, Spring security context
     * must be changed too.<br>
     * Updates also User data of another user, if logged-in has ADMIN rights. In such case,
     * only password or Roles (except ADMIN) can be changed.<br>
     * If all ROLES should be removed, that USER role is inserted.<br>
     * If ADMIN removes his ADMIN role, than it is applied also in Spring authentication object, immediately
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
            
            // logged-in User updated own data - Spring authentication object has to be updated too
            if (isLoggedInUserToManageItself(entity)) { 
                Authentication authentication = authenticationFacade.getAuthentication();
                
                if (authentication != null) {
                    Collection<SimpleGrantedAuthority>  nowAuthorities = (Collection<SimpleGrantedAuthority>) userDetailsService.getGrantedAuthorities(entity);
                        
                    UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(newUserName, newPasswd, nowAuthorities);
                    authenticationFacade.getContext().setAuthentication(newAuthentication);                 
                }
            }
            // User name must be updated (saved) after Spring authentication object update, otherwise isLoggedInUserToManageItself(entity) resolves that 
            // another user (ADMIN) is updating this 'user' as user name was already updated in DB.
            entity.setUserName(newUserName);
            entity.setUpdatedOn(new Timestamp(new Date().getTime()));
        }
        
        log.info("User name {} updated.", user.getUserName());
        return entity;
    }
    
    @Override
    public User updateUser(UserDataDTO userDTO) {
        return updateUser(mapperFacade.map(userDTO,  User.class));
    }
    
    @Override
    public void deleteUserBySSO(String ssoId) {
        usersRepository.deleteByUserName(ssoId);
        log.info("User name {} deleted.", ssoId);
    }
           
    @Override
    public void deleteUserById(Integer id) {
        usersRepository.deleteById(id);
        log.info("User id {} deleted.", id);
    }
 
    @Override
    public List<UserDataDTO> findAllUsers() {
        List<UserDataDTO> usersDTO = mapperFacade.mapAsList(usersRepository.findAll(), UserDataDTO.class);
        
        for (UserDataDTO userDTO : usersDTO) {
            userDTO.setHasADMINRole(hasADMINRole(mapperFacade.map(userDTO,  User.class)));
            userDTO.setToManageItself(isLoggedInUserToManageItself(mapperFacade.map(userDTO,  User.class)));
        }
        
        return usersDTO;
    }
 
    @Override
    public boolean isUserNameUnique(Integer id, String sso) {
        User user = usersRepository.searchByUsername(sso);
        return ( user == null || ((id != null) && (user.getId() == id)));
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
    public boolean isEmailUnique(Integer id, String email) {
        User user = usersRepository.searchByEmail(email);
        return ( user == null || ((id != null) && (user.getId() == id)));
    }
    
    /**
     * Saves new user data to DB. Converts from UserDataDto to standard User
     */
    @Override
    public User save(UserDataDTO registration) {
        User user = new User();
        
        user.setUserName(registration.getUserName());
        user.setFirstName(registration.getFirstName());
        user.setLastName(registration.getLastName());
        user.setEmail(registration.getEmail());
        user.setRegisterEmailConfirmed(false);
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        
        user.setCreatedOn(new Timestamp(new Date().getTime()));
        
        user.setUpdatedSites(0);
        user.setCreatedSites(0);
        user.setDeletedSites(0);    
        
        Set<UserProfile> userProfiles = new HashSet<UserProfile>();
        // Only basic USER role can be assigned to commom user 
        userProfiles.add(userProfileRepository.searchByType("USER"));
        user.setUserProfiles(userProfiles);
        
        log.info("Saving new user name {}", user.getUserName());
        return usersRepository.save(user);
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
        User loggedInUser = getCurrentLoggedInUser();
        return loggedInUser != null && loggedInUser.getId() == user.getId();        
    }

    @Override
    public boolean isADMINloggedIn() {
        User loggedInUser = getCurrentLoggedInUser();
        return loggedInUser != null && hasADMINRole(loggedInUser);
    }
    
    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = authenticationFacade.getAuthentication();
        return findByUserName(authentication.getName());
    }

    /** Methods for E-mail verification tokens **/
    
    @Override
    public void saveVerifiedRegisteredUser(User user, String token) {
        user.setRegisterEmailConfirmed(true);
        // Deletes already used confirmation token from DB. This makes it invalid.
        tokenRepository.deleteByToken(token);
        
        Set<UserProfile> currentUserProfiles = user.getUserProfiles();
        // E-mail confirmed, higher privileges can be added to User
        currentUserProfiles.add(userProfileRepository.searchByType("DBA"));
        user.setUserProfiles(currentUserProfiles);
        usersRepository.save(user);
    }

    @Override
    public User getUserByToken(String verificationToken) {
        User user = tokenRepository.findByToken(verificationToken).getUser();
        return user;
    }

}