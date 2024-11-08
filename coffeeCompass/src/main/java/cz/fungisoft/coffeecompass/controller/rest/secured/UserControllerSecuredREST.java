
package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.controller.rest.UsersControllerPublicREST;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.user.UserService;

/**
 * REST varianta Controlleru, ktery obsluhuje http pozadavky na praci s User objektem.<br>
 * Pro pouziti z web clienta napr. z Angular2 aplikace nebo z mobilni app.
 * <p>
 * Pro pouziti z mobilni aplikace je urcen take {@link UsersControllerPublicREST}
 * 
 * @author Michal Vaclavek
 *
 */
@Tag(name = "UserSecured", description = "Users administration")
@RestController
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/secured/user")
public class UserControllerSecuredREST {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerSecuredREST.class); 
    
    private final UserService userService;
     
    private final UserSecurityService userSecurityService;
    
     /**
      * Vyuziti Dependency Injection pomoci konstruktoru. Jde o preferovany zpusob (na rozdil od @Autowired na atributu nebo setteru)       
      * @param userService
      */
    @Autowired
    public UserControllerSecuredREST(UserService userService,
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
        return ResponseEntity.ok(users);
    }
  
    // ------------------- Retrieve Single User -------------------------------------------------------- //
      
    @GetMapping("/{extId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("extId") String id) {
        logger.info("Fetching User with id {}", id);
        Optional<UserDTO> user = userService.findByExtIdToTransfer(id);
        if (user.isEmpty()) {
            logger.info("User with id {} not found.", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }
    
    @GetMapping("/{userName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("userName") String userName) {
        logger.info("Fetching User with username '{}'", userName);
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(userName); 
        if (currentUser.isEmpty()) {
            logger.info("User with username '{}' not found.", userName);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(currentUser.get(), HttpStatus.OK);
    }
  
    // ------------------- Update a User -------------------------------------------------------- //
      
    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody SignUpAndLoginRESTDto updateUserRequest) {
        logger.info("Updating User name: '{}' via REST api.", updateUserRequest.getUserName());
          
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(updateUserRequest.getUserName());        
          
        if (currentUser.isEmpty()) {
            logger.info("User with username '{}' not found.", updateUserRequest.getUserName());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }        
          
        UserDTO updatedUser = userService.updateRESTUser(updateUserRequest);
        
        if (updatedUser != null) {
            Optional<UserDTO> userDTO = userService.findByExtIdToTransfer(updatedUser.getExtId());

            return userDTO.map(userDTO1 -> new ResponseEntity<>(userDTO1, HttpStatus.OK))
                          .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }
        
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  
    // ------------------- Delete a User -------------------------------------------------------- //
      
    @DeleteMapping(("/delete/{userName}"))
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public String deleteUserByName(@PathVariable("userName") String userName) {
        logger.info("Fetching & Deleting User with username {} via REST api.", userName);
  
        Optional<User> currentUser = userService.findByUserName(userName);
        if (currentUser.isEmpty()) {
            logger.info("Unable to delete. User with username '{}' not found.", userName);
            throw new ResourceNotFoundException("User", "username", userName);
        } 
  
        userService.deleteUserById(currentUser.get().getId());
        return userName;
    }
    
    @DeleteMapping(("/delete/id/{userExtId}"))
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public String deleteUserById(@PathVariable("userId") String userExtId) {
        logger.info("Fetching & Deleting User with userID {} via REST api.", userExtId);
  
        Optional<User> user = userService.findByExtId(userExtId);
        if (user.isEmpty()) {
            logger.info("Unable to delete. User with ID='{}' not found.", userExtId);
            throw new ResourceNotFoundException("User", "userId", userExtId);
        } 
  
        userService.deleteUserById(user.get().getId());
        return userExtId;
    }
    
    
    /** Gets the current REST User loged-in **/
    
    @GetMapping("/current")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO getCurrent(@AuthenticationPrincipal final String userName) {
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(userName);
        return currentUser.orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));
        
    }
    
    /**
     * REST logout by username. Kept because of backward compatibility.
     * 
     * @param userName
     * @return
     */
    @GetMapping("/logout")
    public boolean logout(@AuthenticationPrincipal final String userName) {
        userSecurityService.logout(userName);
        return true;
    }
    
    /**
     * REST logout by userID
     * 
     * @param userExtId
     * @return
     */
    @GetMapping("/logout/{userExtId}")
    public boolean logoutByUserId(@PathVariable("userExtId") String userExtId) {
        Optional<User> user = userService.findByExtId(userExtId);
        if (user.isPresent()) {
            userSecurityService.logout(user.get().getUserName());
            return true;
        } else {
            return false;
        }
    }
}
