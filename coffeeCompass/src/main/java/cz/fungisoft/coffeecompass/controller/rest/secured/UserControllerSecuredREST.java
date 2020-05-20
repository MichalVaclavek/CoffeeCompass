
package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.rest.SignUpAndLoginRESTDto;
import cz.fungisoft.coffeecompass.controller.rest.UsersControllerPublicREST;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
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
@Api // Swagger
@RequestMapping("/rest/secured/user")
@RestController
public class UserControllerSecuredREST
{  
    private static final Logger logger = LoggerFactory.getLogger(UserControllerSecuredREST.class); 
    
    private UserService userService;
     
    private UserSecurityService userSecurityService;
    
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
        return new ResponseEntity<List<UserDTO>>(users,  HttpStatus.OK);
    }
  
    // ------------------- Retrieve Single User -------------------------------------------------------- //
      
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        logger.info("Fetching User with id " + id);
        Optional<UserDTO> user = userService.findByIdToTransfer(id);
        if (!user.isPresent()) {
            logger.info("User with id " + id + " not found");
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserDTO>(user.get(), HttpStatus.OK);
    }
    
    @GetMapping("/{userName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable("userName") String userName) {
        logger.info("Fetching User with username '{}'", userName);
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(userName); 
        if (!currentUser.isPresent()) {
            logger.info("User with username '{}' not found.", userName);
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserDTO>(currentUser.get(), HttpStatus.OK);
    }
  
    // ------------------- Update a User -------------------------------------------------------- //
      
    @PutMapping
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
      
    @DeleteMapping(("/delete/{userName}"))
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> deleteUserByName(@PathVariable("userName") String userName) {
        logger.info("Fetching & Deleting User with username {} ", userName);
  
        Optional<User> currentUser = userService.findByUserName(userName);
        if (!currentUser.isPresent()) {
            logger.info("Unable to delete. User with username '{}' not found.", userName);
            throw new ResourceNotFoundException("User", "username", userName);
        } 
  
        userService.deleteUserById(currentUser.get().getId());
        return new ResponseEntity<String>(userName, HttpStatus.OK);
    }
    
    @DeleteMapping(("/delete/id/{userId}"))
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> deleteUserById(@PathVariable("userId") long userId) {
        logger.info("Fetching & Deleting User with userID {} ", userId);
  
        Optional<User> user = userService.findById(userId);
        if (!user.isPresent()) {
            logger.info("Unable to delete. User with ID='{}' not found.", userId);
            throw new ResourceNotFoundException("User", "userId", userId);
        } 
  
        userService.deleteUserById(user.get().getId());
        return new ResponseEntity<Long>(userId, HttpStatus.OK);
    }
    
    
    /** Gets the current REST User loged-in **/
    
    @GetMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> getCurrent(@AuthenticationPrincipal final String userName) {
        Optional<UserDTO> currentUser = userService.findByUserNameToTransfer(userName);
        return new ResponseEntity<UserDTO>(currentUser.orElseThrow(() -> new ResourceNotFoundException("User", "username", userName)), HttpStatus.OK);
        
    }
    
    /**
     * REST logout by username. Kept because of backward compatibility.
     * 
     * @param user
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
     * @param user
     * @return
     */
    @GetMapping("/logout/{userId}")
    public boolean logoutByUserId(@PathVariable("userId") long userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            userSecurityService.logout(user.get().getUserName());
            return true;
        } else {
            return false;
        }
    }
  
}
