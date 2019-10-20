package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.service.UserService;
import io.swagger.annotations.Api;

/**
 * REST varianta Controlleru, ktery obsluhuje http pozadavky na praci s User objektem 
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@RequestMapping("/rest/user") // uvadi se, pokud vsechny dotazy v kontroleru maji zacinat timto retezcem
@RestController
public class UserControllerREST
{  
    private static final Logger logger = LoggerFactory.getLogger(UserControllerREST.class); 
    
    private UserService userService;
     
     /**
      * Vyuziti Dependency Injection pomoci konstruktoru. Jde o preferovany zpusob (na rozdil od @Autowired na atributu nebo setteru)       
      * @param userService
      */
    @Autowired
    public UserControllerREST(UserService userService /*, UserProfileService userProfileService*/) {
        super();
        this.userService = userService;
    }


    /*
    @Autowired
    MessageSource messageSource;
 
    @Autowired
    PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;
     
    @Autowired
    AuthenticationTrustResolver authenticationTrustResolver;
    */
    
    // ------------------- Retrieve All Users --------------------------------------------------------//
    
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> listAllUsers() {
        
        List<UserDTO> users = userService.findAllUsers();
       
        logger.info("All users retrieved: {}", users.size());
        return new ResponseEntity<List<UserDTO>>(users,  HttpStatus.OK);
    }
  
    // ------------------- Retrieve Single User -------------------------------------------------------- //
      
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable("id") Long id) {
        logger.info("Fetching User with id " + id);
        UserDTO user = userService.findByIdToTransfer(id);
        if (user == null) {
            logger.info("User with id " + id + " not found");
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserDTO>(user, HttpStatus.OK);
    }
  
      
    // ------------------- Create a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO user, UriComponentsBuilder ucBuilder) {
        logger.info("Creating User " + user.getUserName());
  
        if (userService.isUserNameUnique(null, user.getUserName())) {
            logger.info("A User with name " + user.getUserName() + " already exist");
            return new ResponseEntity<UserDTO>(HttpStatus.CONFLICT);
        }
  
        User savedUser = userService.save(user);

        ResponseEntity<UserDTO> response;
        
        response = (savedUser != null) ? new ResponseEntity<UserDTO>(userService.findByIdToTransfer(savedUser.getId()), HttpStatus.CREATED)
                                       : new ResponseEntity<UserDTO>(HttpStatus.METHOD_FAILURE);
        return response;
    }
       
    // ------------------- Update a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        logger.info("Updating User " + id);
          
        User currentUser = userService.findById(id);        
          
        if (currentUser == null) {
            logger.info("User with id " + id + " not found");
            return new ResponseEntity<UserDTO>(HttpStatus.NOT_FOUND);
        }        
          
        userService.updateUser(currentUser);
        
        return new ResponseEntity<UserDTO>(userService.findByIdToTransfer(id), HttpStatus.OK);
    }
  
    // ------------------- Delete a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteUser(@PathVariable("id") Long id) {
        logger.info("Fetching & Deleting User with id " + id);
  
        UserDTO user = userService.findByIdToTransfer(id);
        if (user == null) {
            logger.info("Unable to delete. User with id " + id + " not found");
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
  
        userService.deleteUserById(id);
        return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
    }
  
      
    //------------------- Delete All Users --------------------------------------------------------
      /*
    @RequestMapping(value = "/user/", method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteAllUsers()
    {
        System.out.println("Deleting All Users");
  
        userService.deleteAllUsers();
        return new ResponseEntity<User>(HttpStatus.NO_CONTENT);
    }
  */
    
}
