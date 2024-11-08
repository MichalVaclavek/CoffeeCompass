
package cz.fungisoft.coffeecompass.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.controller.models.DeleteUserAccountModel;
import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.exceptions.UserNotFoundException;
import cz.fungisoft.coffeecompass.listeners.OnRegistrationCompleteEvent;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;
import cz.fungisoft.coffeecompass.service.tokens.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.user.UserProfileService;
import cz.fungisoft.coffeecompass.service.user.UserSecurityService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;

/**
 * Controller to serve all the requests for login, creating, modifying of a Users, mainly on the<br>
 * user_registration.html, users_info.html pages.
 * 
 * @author Michal Václavek
 *
 */
@RequestMapping("/user")
@Controller
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    
    private static final String USER_REGISTRATION_VIEW = "user_registration";
    
    private static final String USER_NAME_ATTRIB_KEY = "userName";
    private static final String USER_DELETE_FAIL_ATTRIB_KEY = "userDeleteFailure";
    
    
    private final UserService userService;
    
    private final UserSecurityService userSecurityService;
    
    private final UserProfileService userProfileService;
    
    private final MessageSource messages;
    
    private final ICommentService commentsService;
    
    private final CoffeeSiteService coffeeSiteService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private final TokenCreateAndSendEmailService tokenCreateAndSendEmailService;

    @Value("${redirect.to.https.home}")
    private String redirectToHome;
    
    
    /* // For future use
    @Autowired
    PersistentTokenBasedRememberMeServices persistentTokenBasedRememberMeServices;
    */
     
     /**
      * Vyuziti Dependency Injection pomoci konstruktoru. Jde o preferovany zpusob (na rozdil od @Autowired na atributu nebo setteru)       
      * @param userService
      */
    @Autowired
    public UserController(UserService userService,
                          UserSecurityService userSecurityService,
                          UserProfileService userProfileService,
                          TokenCreateAndSendEmailService verificationTokenService,
                          ICommentService commentsService,
                          CoffeeSiteService coffeeSiteService,
                          MessageSource messages) {
        super();
        this.userService = userService;
        this.userSecurityService = userSecurityService;
        this.userProfileService = userProfileService;
        this.tokenCreateAndSendEmailService = verificationTokenService;
        this.commentsService = commentsService;
        this.coffeeSiteService = coffeeSiteService;
        this.messages = messages;
    }

    
    // ---------------- Retrieve All Users ---------------- //
    
    @GetMapping("/all") 
    public ModelAndView listAllUsers() {
        
        ModelAndView mav = new ModelAndView();
        
        List<UserDTO> users = userService.findAllUsers();
        mav.addObject("allUsers", users);
        mav.setViewName("users_info");
    
        // Test loogging
        logger.info("All users retrieved: {}", users.size());
        return mav;
    }
  
    
    /**
     * Request to get Form for registering new user.
     * 
     * @param user
     * @param model
     * @return
     */
    @GetMapping("/register")
    public ModelAndView showRegistrationForm(final UserDTO user, Model model) {
        
        ModelAndView mav = new ModelAndView();
        
//        user.setId(0L); // set Id to 0 of type long
//        user.setExtId(0L); // set Id to 0 of type long
        // we need to know, that this is a user managing its own profile
        user.setToManageItself(true);
        mav.addObject("user", user);
        mav.setViewName(USER_REGISTRATION_VIEW);
        return mav;
    }

    /**
     * Obslouzi pozadavek na vytvoreni noveho uzivatele/uctu.<br>
     * Vytvorit uzivatele muze pouze neprihlaseny uzivatel.<br>
     * Po uspesne registraci je uzivatel automaticky logged-in.<br>
     * <p>
     * Je potreba provest kontrolu/validaci password, ktera je v prisusnem DTO vypnuta (v UserDataDto nema zadnou jakarta.validation.constraints.)
     * protoze stejny UserDataDto objekt je pouzit i pro modifikaci, kdy prazdne heslo znamena, ze se heslo nepozaduje menit.
     * <p>
     * Pro modifikace je volana PUT metoda viz nize.
     * 
     * @param userDto - objekt, ktery vytvori kontroler/Spring z dat na strance register_user
     * @param result - objekt obsahujici vysledky validace jednotlivych fields objektu userDto
     * @param model - objekt Springu s informacemi o objektech na strance a o prislusnem View, ktere tyto objekty zobrazuje.
     * @param request - potrebny pro ziskani aktualni URL pro verifikacni odkaz (verifikace e-mailu uzivatele).
     * @param attr - slouzi k predavani atributu do dalsich controleru v pripade presmerovani
     * 
     * @return nova stranka potvrzuji uspesnou registraci, v tomto pripade je to home stranka
     */
    @PostMapping("/registration")
    public ModelAndView registerUserAccountWithEmail(@ModelAttribute("user")
                                                     @Valid
                                                     UserDTO userDto,
                                                     BindingResult result,
                                                     ModelMap model,
                                                     HttpServletRequest request,
                                                     RedirectAttributes attr) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName(USER_REGISTRATION_VIEW);
        
        if (userDto.getExtId() == null) { // Jde o noveho usera k registraci
            Optional<User> existing = userService.findByUserName(userDto.getUserName());
            
            if (existing.isPresent()) {
                result.rejectValue(USER_NAME_ATTRIB_KEY,"error.user.name.used", "There is already an account registered with that user name.");
            }
    
            // Kontrola i na e-mail adresu, pokud je zadana
            if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) { // should be true as input of e-mail has been validated
                if (!userService.isEmailUnique(userDto.getExtId(), userDto.getEmail())) {
                    result.rejectValue("email", "error.user.emailused", "There is already an account registered with that e-mail address.");
                }
            }
        }
        
        if (userDto.getPassword().isEmpty()) {
            result.rejectValue("password", "error.user.password.empty");
        }
        else {
            if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.user.password.confirm", "Confirmation password does not match password.");
            }
        }
        
        if (result.hasErrors()) { // In case of error, show the user_reg. page again with error labels
            userDto.setToManageItself(true);
            mav.addObject(userDto);
            return mav;
        }
        
        boolean userCreateSuccess;
        User newUser = null;
        
        if (userDto.getExtId() == null) {
            newUser = userService.save(userDto);
        }
        
        if (newUser != null) {
            userCreateSuccess = true;
            
            // User can be logged-in now. Used userDto password as it is not encrypted yet
            userSecurityService.authWithPassword(newUser, userDto.getPassword());
            
            mav.setViewName("redirect:/home/?lang=" + request.getLocale().getLanguage());
            
            attr.addFlashAttribute(USER_NAME_ATTRIB_KEY, userDto.getUserName());
            attr.addFlashAttribute("userCreateSuccess", userCreateSuccess);
            
            if (!newUser.getEmail().isEmpty()) { // non empty, valid e-mail available for user (UserDataDTO validated e-mail using annotation)
                try {
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale())); // invokes sending verification e-mail to the new user
                    attr.addFlashAttribute("verificationEmailSent", true);
                }
                catch(MailException e) {
                    logger.error("MailException: {}", e.getMessage());
                    
                    String sendEmailErrorMessage = messages.getMessage("register.verificationemail.sent.failure", null, request.getLocale());
                    attr.addFlashAttribute("sendVerificationEmailErrorMessage", sendEmailErrorMessage);
                }
            }
        }
        
        return mav;
    }
    
    // ------------------- Update a User -------------------------------------------------------- //
    
    /**
     * //TODO change usage of userName in Controllers to userID!!! userName can be changed, threfore cannot be used fo identification
     * 
     * This method will provide the page with Form to update an existing user.
     * 
     * @param userName - user name of the User to be edited here.
     * @param firstOAuth2Login - indicates if this is the very first edit after social login of a new User.<br>
     *                           If true, then welcome message (and registration finish request) is shown to user on edit page.
     * @param model
     * @param locale
     * @return
     */
    @GetMapping("/edit/") // napr. http://coffeecompass.cz/user/edit/?userName=Michal Vaclavek&firstOAuth2Login=true
    public ModelAndView getEditUserForm(@RequestParam(value=USER_NAME_ATTRIB_KEY, defaultValue="") String userName,
                                        @RequestParam(value="firstOAuth2Login", defaultValue="") String firstOAuth2Login,
                                        ModelMap model,
                                        Locale locale) {    
    
        ModelAndView mav = new ModelAndView();
        mav.setViewName(USER_REGISTRATION_VIEW);

        Optional<UserDTO> user = userService.findByUserNameToTransfer(userName);
        if (user.isPresent()) {
            mav.addObject("user", user.get());
        } else {
            logger.error("User name {} not found.", userName);
            throw new UserNotFoundException("User name " + userName + " not found.");
        }
        
        if ("true".equals(firstOAuth2Login)) {
            // OAuth2 provider name with first letter in upper case
            String oAuth2ProviderName = user.get().getAuthProvider().substring(0, 1).toUpperCase() + user.get().getAuthProvider().substring(1);
            mav.addObject("socialLoginStillAvailableMessage", messages.getMessage("user.register.social.firstlogin.message.socialloginavailable", new Object[] {oAuth2ProviderName}, locale));
        }
        mav.addObject("firstOAuth2Login", "true".equals(firstOAuth2Login));
        mav.addObject(USER_NAME_ATTRIB_KEY, userName);
        
        return mav;
    }
    
    /**
     * Obslouzi pozadavek na modifikaci uzivatele/uctu.
     * <p>
     * Editaci muze provest pouze prihlaseny uzivatel a to bud normalni, non ADMIN user nebo i ADMIN, ktery edituje svuj ucet,
     * nebo ADMIN, ktery edituje jiny, non ADMIN ucet.
     * <p>
     * V obou pripadech je potreba provest kontrolu password, ktera je vypnuta (v UserDataDto nema anotaci @NotEmpty, ani jinou).
     * V pripade modifikace stavajiciho uzivatele totiz mohou byt obe polozky prazdne. Znamena to, ze heslo se nepozaduje menit. 
     * <p>
     * Pokud ADMIN meni jineho non ADMIN usera, muze menit pouze jeho password a ROLES.
     * <p>
     * ADMIN muze u jineho ADMINA editovat heslo a ROLES, krome ADMIN.
     * <p>
     * Pokud User, s jakoukoliv roli, meni svuj ucet, muze menit vsechny udaje, krome ROLES, vyjma ADMINa, ktery muze menit sve ROLES.
     * <p>
     * @param userDto UserDTO object to be modified in DB
     * @param result binding result from View to UserDTO (performed by Thymeleaf?)
     * @param model
     * @param attr pro vlozeni chybovych atributu, pokud se presmerovava pomoci redirect na GET stranku pro zobrazeni validacnich chyb
     * @return
     */
    @PutMapping("/edit-put")
    public ModelAndView editUserAccount(  @ModelAttribute("user")
                                          @Valid 
                                          UserDTO userDto,
                                          BindingResult result,
                                          ModelMap model,
                                          HttpServletRequest request,
                                          RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        mav.setViewName(USER_REGISTRATION_VIEW);
        
        // Neprihlaseny uzivatel muze byt editovany ADMINem - ten muze menit pouze Password a ROLES, pokud nejde taky o ADMIN usera.
        // ADMIN nemuze byt editovan jinym ADMINem.
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser.isPresent()) { // Jde o prihlaseneho uzivatele - muze pokracovat editace
            // Prihlaseny uzivatel meni svoje data - je potreba overit userName a e-mail, ktere se nesmi shodovat s jinym jiz vytvorenym.
            // Pokud jineho usera edituje ADMIN, neni potreba overovat, protoze ADMIn nemuze menit ve formulari ani userName ani e-mail
            if (Objects.equals(userDto.getExtId(), loggedInUser.get().getLongId())) {
                // Tento blok je spravny pouze pokud prihlaseny user meni svoje udaje (je jedno jaky ma Profile)
                if (!loggedInUser.get().getUserName().equalsIgnoreCase(userDto.getUserName())) { // Prihlaseny uzivatel chce zmenit userName
                    // Je uzivatelske jmeno jiz pouzito
                    if (!userService.isUserNameUnique(userDto.getExtId(), userDto.getUserName())) {
                        result.rejectValue(USER_NAME_ATTRIB_KEY, "error.user.name.used", "There is already an account registered with that user name.");
                    }
                }
                
                // Prihlaseny user chce zmenit svoji e-mail adresu - overit jestli je to mozne
                if (!loggedInUser.get().getEmail().equalsIgnoreCase(userDto.getEmail())) { // nova e-mail adresa se lisi nebo je smazana
                    // Je e-mail jiz pouzit ?
                    if (!userService.isEmailUnique(userDto.getExtId(), userDto.getEmail())) {
                        result.rejectValue("email", "error.user.emailused", "There is already an account registered with that e-mail");
                    } 
                }
            }
            
            // Kontrola hesla. Muze byt prazdne. Pokud neni, musi se shodovat s confirmPassword
            if (!userDto.getPassword().isEmpty()
                && !userDto.getPassword().equals(userDto.getConfirmPassword())) {
                    result.rejectValue("confirmPassword", "error.user.password.confirm", "Confirmation password does not match password.");
            }
        }
        
        if (result.hasErrors()) { // In case of error, show the user edit page again with errors
            // Prihlaseny uzivatel edituje svuj profil - musi se nastavit prislusny flag, aby zustaly ve formulari vsechny plozky editovatelne
            // i v pripade predchozi chyby ve formulari
           if (loggedInUser.isPresent() && Objects.equals(userDto.getExtId(), loggedInUser.get().getLongId())) {
               userDto.setToManageItself(true);
           }
           return mav;
        }
                
        boolean userModifySuccess = false;
        
        User updatedUser = userService.updateUser(userDto);
        String encodedUserName = "";
        
        if (updatedUser != null) {
            userModifySuccess = true;
            if (loggedInUser.isPresent() && updatedUser.getLongId().equals(loggedInUser.get().getLongId())) { // If the user modifies it's own profile
                // Check if the email address is confirmed
                if (!updatedUser.isRegisterEmailConfirmed()
                     && !updatedUser.getEmail().isEmpty()) { // novy email nepotvrzen a neprazdny. Poslat confirm e-mail token
                    try {
                        tokenCreateAndSendEmailService.createAndSendVerificationTokenEmail(updatedUser, request.getLocale());
                        attr.addFlashAttribute("verificationEmailSent", true); // requires processing of the "verificationEmailSent" attr. in "redirect:/user/edit/", see bellow
                    } catch(MailException e) {
                        logger.error("MailException: {}", e.getMessage());
                        
                        String sendEmailErrorMessage = messages.getMessage("register.verificationemail.sent.failure", null, request.getLocale());
                        attr.addFlashAttribute("sendVerificationEmailErrorMessage", sendEmailErrorMessage);
                    }
                }
            }
            try {
                encodedUserName = URLEncoder.encode(updatedUser.getUserName(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.warn("User name URL encoding error. User name {}.", updatedUser.getUserName());
            }
            
            attr.addFlashAttribute(USER_NAME_ATTRIB_KEY, updatedUser.getUserName());
        }
        attr.addFlashAttribute("userModifySuccess", userModifySuccess);
        mav.setViewName("redirect:/home/?userName=" + encodedUserName);
        return mav;
    }
    
    
    // ------------------- Delete a User -------------------------------------------------------- //
      
    /**
     * Creates/prepares form to confirm user account deletition.
     * 
     * @param id
     * @return
     */
    @GetMapping(value = "/delete/")
    public ModelAndView getDeleteUserAndRelatedItemsForm(@RequestParam("userID") String id, RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        
        Optional<User> user = userService.findByExtId(id);
        
        if (user.isPresent()) {
            DeleteUserAccountModel userToDeleteModel = new DeleteUserAccountModel();
            
            userToDeleteModel.setUserId(UUID.fromString(id));
            userToDeleteModel.setUserName(user.get().getUserName());
            userToDeleteModel.setUserToDeleteItself(userService.isLoggedInUserToManageItself(user.get()));
    
            // if user created comments, ...
            if (!commentsService.getAllCommentsFromUser(id).isEmpty()) {
                userToDeleteModel.setDeleteUsersComments(true);
            }
            
            // if user created CoffeeSites ....
            if (!coffeeSiteService.findAllFromUser(user.get()).isEmpty()) {
                userToDeleteModel.setDeleteUsersCoffeeSites(true);
            }
            
            mav.addObject("userDataModelToDelete", userToDeleteModel);
            mav.setViewName("user_delete");
            
        } else {
            logger.error("Unable to delete user with id {}. Probably already deleted.", id);
            return redirectToHomeAfterFailedUserDelete(id, attr);
        }
        return mav;
    }
    
    /**
     * Deletes user account, without verifiyng if there are any related records in another
     * tables/entities. Usualy called from the page listing all the user accounts by ADMIN
     * user.
     * 
     * @param extId - id of the user to be deleted
     * @return
     */
    @DeleteMapping(value = "/delete/{extId}")
    public ModelAndView deleteUser(@PathVariable("extId") String extId, RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        
        Optional<UserDTO> user = userService.findByExtIdToTransfer(extId);
        boolean deleteOK = false;
        
        if (user.isPresent()) {
            try {
                userService.deleteUserById(extId);
                deleteOK = true;
            } catch (Exception ex) {
                logger.error("Unable to delete user with id {}. Probably already deleted. Exception: {}", extId, ex.getMessage());
            }
        }
        
        if (deleteOK) {
            mav.setViewName("redirect:/user/all");
            return mav;
        } else {
            return redirectToHomeAfterFailedUserDelete(extId, attr);
        }
    }
    
    
    /**
     * Deletes user and all related data (comments, coffee sites, ...) if requested.<br>
     * If related data are not requested to delete, it deletes user's data in DB,<br>
     * except userID and username. So, the user record is kept to allow all<br>
     * related items be link to user's record.  
     * 
     * @param userDataToDelete
     * @return
     */
    @DeleteMapping(value = "/delete/")
    public ModelAndView deleteUserAndRelatedItems(@ModelAttribute("userDataModelToDelete") DeleteUserAccountModel userDataToDelete,
                                                  RedirectAttributes attr) {
        ModelAndView mav = new ModelAndView();

        Optional<UserDTO> userDto = userService.findByExtIdToTransfer(userDataToDelete.getUserId());
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        
        String userName = "";
       
        if (userDto.isPresent() && loggedInUser.isPresent()) {
            
           userName = userDto.get().getUserName();
           
           // Prihlaseny uzivatel maze svoje data?
           // Pokud jineho usera maze ADMIN, neodhlasovat z app
           if (userDto.get().getExtId().equals(loggedInUser.get().getLongId())
                   && !userService.isADMINloggedIn()) { 
               userSecurityService.logout();
           }
            
           // delete user's coffee sites if requested
           if (userDataToDelete.isDeleteUsersCoffeeSites()) {
               coffeeSiteService.deleteCoffeeSitesFromUser(userDataToDelete.getUserId());
           }
            
           // delete user's comments if requested
           if (userDataToDelete.isDeleteUsersComments()) {
               commentsService.deleteAllCommentsFromUser(userDataToDelete.getUserId());
           }

           // Delete user's email verification tokens
           userService.findByExtId(userDataToDelete.getUserId())
                      .ifPresent(tokenCreateAndSendEmailService::deleteRegistrationTokenByUser);

           if (commentsService.getAllCommentsFromUser(userDto.get().getExtId()).isEmpty()
                   && coffeeSiteService.findAllFromUserName(userName).isEmpty()) { // user's comments and CoffeeSites deleted, now User can be deleted too
               try {
                   userService.deleteUserById(userDataToDelete.getUserId());
               } catch (Exception ex) {
                   logger.error("Unable to delete user with id {}. Probably already deleted.", userDataToDelete.getUserId());
                   attr.addFlashAttribute(USER_DELETE_FAIL_ATTRIB_KEY, true);
                   return redirectToHomeAfterFailedUserDelete(userDataToDelete.getUserId().toString(), attr);
               }
           } else { // clear user's data as either user's CoffeeSites or comments are not deleted
               userService.clearUserDataById(userDataToDelete.getUserId());   
           }
        } else {
           logger.info("Unable to delete. User with id {} not found or not registered currently.", userDataToDelete.getUserId());
           attr.addFlashAttribute(USER_DELETE_FAIL_ATTRIB_KEY, true);
           return redirectToHomeAfterFailedUserDelete(userDataToDelete.getUserId().toString(), attr);
        }
       
        attr.addFlashAttribute("userDeleteSuccess", true);
        attr.addFlashAttribute(USER_NAME_ATTRIB_KEY, userName);
        mav.setViewName("redirect:/home/");
        return mav;
    }
    
    /**
     * Helper method to simplify call to home page after User delete cannot be performed.
     * 
     * @param extId - user's id
     * @param attr
     * @return
     */
    private ModelAndView redirectToHomeAfterFailedUserDelete(String extId, RedirectAttributes attr) {
        
        ModelAndView mav = new ModelAndView();
        
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        
        // Uzivatel, pravdepodobne jiz smazany z jineho zarizeni, se pokousi smazat svuj ucet
        // z aktualniho zarizeni, kde vypada stale prohlaseny. Je potreba provest logout explicitne.
        if (loggedInUser.isEmpty()) {
            userSecurityService.logout();
        }
        
        // Prihlaseny uzivatel maze svoje data?
        // Pokud jineho usera maze ADMIN, neodhlasovat z app
        if (loggedInUser.isPresent() && extId.equals(loggedInUser.get().getLongId().toString())
                && !userService.isADMINloggedIn()) { 
            userSecurityService.logout();
        }
        
        logger.info("Unable to delete. User with id {} not found", extId);
        attr.addFlashAttribute(USER_NAME_ATTRIB_KEY, extId);
        attr.addFlashAttribute(USER_DELETE_FAIL_ATTRIB_KEY, true);
        
        mav.setViewName("redirect:/home/");
        
        return mav;
    }
  
    /**
     * Provides all configured ROLE names to allow modification of the user's ROLEs.
     * 
     * @return all ROLE names to Model.
     */
    @ModelAttribute("allUserProfiles")
    public List<UserProfile> populateUserProfiles() {
        return userProfileService.findAll();
    }
}