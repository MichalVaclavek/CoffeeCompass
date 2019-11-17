package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.controller.rest.UsersControllerPublicREST;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exception.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.UserSecurityService;
import cz.fungisoft.coffeecompass.service.UserService;
import io.swagger.annotations.Api;

/**
 * REST varianta Controlleru, ktery obsluhuje http pozadavky na praci s User objektem.<br>
 * Pro pouziti z web clienta napr. z Angular2 aplikace nebo z mobilni app.
 * <p>
 * Pro pouziti z mobilni aplikace je urcen take {@link UsersControllerPublicREST}
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@RequestMapping("/rest/secured/user") // uvadi se, pokud vsechny dotazy v kontroleru maji zacinat timto retezcem
@RestController
public class UserControllerSecuredREST
{  
    private static final Logger logger = LoggerFactory.getLogger(UserControllerSecuredREST.class); 
    
    private UserService userService;
     
    //private CustomRESTUserAuthenticationService userSecurityService;
    private UserSecurityService userSecurityService;
    
     /**
      * Vyuziti Dependency Injection pomoci konstruktoru. Jde o preferovany zpusob (na rozdil od @Autowired na atributu nebo setteru)       
      * @param userService
      */
    @Autowired
    public UserControllerSecuredREST(UserService userService,
                                     //@Qualifier("jwtTokenUserAuthenticationService")
                                    UserSecurityService userSecurityService) {
        super();
        this.userService = userService;
        this.userSecurityService = userSecurityService;
    }

    // ------------------- Retrieve All Users --------------------------------------------------------//
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> listAllUsers() {
        
        List<UserDTO> users = userService.findAllUsers();
       
        logger.info("All users retrieved: {}", users.size());
        return new ResponseEntity<List<UserDTO>>(users,  HttpStatus.OK);
    }
  
    // ------------------- Retrieve Single User -------------------------------------------------------- //
      
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        logger.info("Fetching User with id " + id);
        Optional<UserDTO> user = userService.findByIdToTransfer(id);
        if (user.isPresent()) {
            logger.info("User with id " + id + " not found");
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserDTO>(user.get(), HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("id") String userName) {
        logger.info("Fetching User with username '{}'", userName);
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(userName); 
        if (!currentUser.isPresent()) {
            logger.info("User with username '{}' not found.", userName);
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserDTO>(currentUser.get(), HttpStatus.OK);
    }
  
    // ------------------- Create a User -------------------------------------------------------- //
      
//    @RequestMapping(value = "/", method = RequestMethod.POST)
//    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO user, UriComponentsBuilder ucBuilder) {
//        logger.info("Creating User " + user.getUserName());
//  
//        if (userService.isUserNameUnique(null, user.getUserName())) {
//            logger.info("A User with name " + user.getUserName() + " already exist");
//            return new ResponseEntity<UserDTO>(HttpStatus.CONFLICT);
//        }
//  
//        User savedUser = userService.save(user);
//
//        ResponseEntity<UserDTO> response;
//        response = (savedUser != null) ? new ResponseEntity<UserDTO>(userService.findByIdToTransfer(savedUser.getId()), HttpStatus.CREATED)
//                                       : new ResponseEntity<UserDTO>(HttpStatus.METHOD_FAILURE);
//        return response;
//    }
       
    // ------------------- Update a User -------------------------------------------------------- //
      
    @RequestMapping(method = RequestMethod.PUT)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody SignUpAndLoginRESTDto updateUserRequest) {
        logger.info("Updating User name: '{}'", updateUserRequest.getUserName());
          
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(updateUserRequest.getUserName());        
          
        if (!currentUser.isPresent()) {
            logger.info("User with username '{}' not found.", updateUserRequest);
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }        
          
        userService.updateUser(currentUser.get());
        return new ResponseEntity<UserDTO>(currentUser.get(), HttpStatus.OK);
    }
  
    // ------------------- Delete a User -------------------------------------------------------- //
      
    @RequestMapping(method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> deleteUser(@PathVariable("id") String userName) {
        logger.info("Fetching & Deleting User with username {} ", userName);
  
        Optional<User> currentUser = userService.findByUserName(userName);
        if (!currentUser.isPresent()) {
            logger.info("Unable to delete. User with username '{}' not found.", userName);
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
  
        userService.deleteUserById(currentUser.get().getId());
        return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
    }
    
    /** Gets the current REST User loged-in **/
    
    @GetMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public UserDTO getCurrent(@AuthenticationPrincipal final String userName) {
        return userService.findByUserNameToTransfer(userName).orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));
    }
    
    /**
     * REST logout
     * 
     * @param user
     * @return
     */
    @GetMapping("/logout")
    public boolean logout(@AuthenticationPrincipal final String userName) {
        userSecurityService.logout(userName);
        return true;
    }
  
}
