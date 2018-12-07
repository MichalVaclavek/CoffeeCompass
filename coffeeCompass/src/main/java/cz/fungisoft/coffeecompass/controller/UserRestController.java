package cz.fungisoft.coffeecompass.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.dto.UserDataDto;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.service.UserProfileService;
import cz.fungisoft.coffeecompass.service.UserService;
import io.swagger.annotations.Api;

@Api // Anotace Swagger
//@RequestMapping("/user") // uvadi se, pokud vsechny dotazy v kontroleru maji zacinat timto retezcem
//@RestController
public class UserRestController
{  
    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class); 
    
    private UserService userService;
     
     /**
      * Vyuziti Dependency Injection pomoci konstruktoru. Jde o preferovany zpusob (na rozdil od @Autowired na atributu nebo setteru)       
      * @param userService
      */
    @Autowired
    public UserRestController(UserService userService /*, UserProfileService userProfileService*/)
    {
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
    
//  @RequestMapping(value = "/users", method = RequestMethod.GET)  
    @GetMapping("/all") 
    public ModelAndView listAllUsers()
    {
        ModelAndView mav = new ModelAndView();
        
        List<UserDataDto> users = userService.findAllUsers();
        
        mav.addObject("allUsers", users);
        mav.setViewName("users_info");
    
        //Test loogging
        logger.info("All users retrieved: {}", users.size());
        return mav;
    }
  
  
    // ------------------- Retrieve Single User -------------------------------------------------------- //
      
//    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE,MediaType.APPLICATION_XML_VALUE})
    @GetMapping("/{id}")
    public ResponseEntity<UserDataDto> getUser(@PathVariable("id") Integer id)
    {
        logger.info("Fetching User with id " + id);
        UserDataDto user = userService.findByIdToTransfer(id);
        if (user == null)
        {
            logger.info("User with id " + id + " not found");
            return new ResponseEntity<UserDataDto>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<UserDataDto>(user, HttpStatus.OK);
    }
  
      
    // ------------------- Create a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Void> createUser(@RequestBody User user, UriComponentsBuilder ucBuilder)
    {
        logger.info("Creating User " + user.getUserName());
  
        if (userService.isUserNameUnique(null, user.getUserName())) {
            logger.info("A User with name " + user.getUserName() + " already exist");
            return new ResponseEntity<Void>(HttpStatus.CONFLICT);
        }
  
        userService.saveUser(user);
  
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(user.getId()).toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }
       
    // ------------------- Update a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<UserDataDto> updateUser(@PathVariable("id") Integer id, @RequestBody User user)
    {
        logger.info("Updating User " + id);
          
        User currentUser = userService.findById(id);        
          
        if (currentUser == null)
        {
            logger.info("User with id " + id + " not found");
            return new ResponseEntity<UserDataDto>(HttpStatus.NOT_FOUND);
        }        
          
        userService.updateUser(currentUser);
        
        return new ResponseEntity<UserDataDto>(userService.findByIdToTransfer(id), HttpStatus.OK);
    }
  
    // ------------------- Delete a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<User> deleteUser(@PathVariable("id") Integer id)
    {
        logger.info("Fetching & Deleting User with id " + id);
  
        UserDataDto user = userService.findByIdToTransfer(id);
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
