package cz.fungisoft.coffeecompass.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.listeners.OnRegistrationCompleteEvent;
import cz.fungisoft.coffeecompass.service.UserProfileService;
import cz.fungisoft.coffeecompass.service.UserSecurityService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.service.TokenCreateAndSendEmailService;

/**
 * Controller to serve all the requests for login, creating, modifying of a Users, mainly on the<br>
 * user_registration.html, users_info.html pages.
 * 
 * @author Michal Václavek
 *
 */
@RequestMapping("/user") // uvadi se, pokud vsechny dotazy/url requesty v kontroleru maji zacinat timto retezcem
@Controller
public class UserController
{  
    private static final Logger logger = LoggerFactory.getLogger(UserController.class); 
    
    private UserService userService;
    
    private UserSecurityService userSecurityService;
    
    private UserProfileService userProfileService;
    
    private MessageSource messages;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private TokenCreateAndSendEmailService tokenCreateAndSendEmailService;
    
    
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
                          MessageSource messages) {
        super();
        this.userService = userService;
        this.userSecurityService = userSecurityService;
        this.userProfileService = userProfileService;
        this.tokenCreateAndSendEmailService = verificationTokenService;
        this.messages = messages;
    }

    
    // ------------------- Retrieve All Users -------------------------------------------------------- //
    
    @GetMapping("/all") 
    public ModelAndView listAllUsers() {
        
        ModelAndView mav = new ModelAndView();
        
        List<UserDataDTO> users = userService.findAllUsers();
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
    public ModelAndView showRegistrationForm(final UserDataDTO user, Model model) {
        
        ModelAndView mav = new ModelAndView();
        
        mav.addObject("user", user);
        mav.setViewName("user_registration");
        return mav;
    }

    /**
     * Obslouzi pozadavek na vytvoreni noveho uzivatele/uctu.<br>
     * Vytvorit uzivatele muze vytvaret pouze neprihlaseny uzivatel.<br>
     * Po uspesne registraci je uzivatel automaticky loged-in.<br>
     * <p>
     * Je potreba provest kontrolu/validaci password, ktera je v prisusnem DTO vypnuta (v UserDataDto nema zadnou javax.validation.constraints.)
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
    public ModelAndView registerUserAccountWithEmail(@ModelAttribute("user") @Valid UserDataDTO userDto,
                                                      BindingResult result,
                                                      ModelMap model,
                                                      HttpServletRequest request,
                                                      RedirectAttributes attr) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName("user_registration");
        
        if (userDto.getId() == 0) { // Jde o noveho usera k registraci
            User existing = userService.findByUserName(userDto.getUserName());
            
            if (existing != null) {
                result.rejectValue("userName","error.user.name.used", "There is already an account registered with that user name.");
            }
    
            // Kontrola i na e-mail adresu, pokud je zadana
            if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) { // should be true as input of e-mail has been validated
                
                existing = userService.findByEmail(userDto.getEmail());
                if (existing != null) {
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
            mav.addObject(userDto);
            return mav;
        }
        
        boolean userCreateSuccess = false;
        User newUser = null;
        
        if (userDto.getId() == 0) {
            newUser = userService.save(userDto);
        }
        
        if (newUser != null) {
            userCreateSuccess = true;
            
            // User can be logged-in now. Used userDto password as it is not encrypted yet
            userSecurityService.authWithPassword(newUser, userDto.getPassword());
            
            mav.setViewName("redirect:/home/?lang=" + request.getLocale().getLanguage());
            
            attr.addFlashAttribute("userName", userDto.getUserName());
            attr.addFlashAttribute("userCreateSuccess", userCreateSuccess);
            
            if (!newUser.getEmail().isEmpty()) { // non empty, valid e-mail available for user (UserDataDTO validated e-mail using annotation)
                try {
                    String appUrl = "http://" + request.getServerName() +  ":" + request.getServerPort() +  request.getContextPath();
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(newUser, request.getLocale(), appUrl));
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
     * This method will provide the page with Form to update an existing user.
     */
    @GetMapping("/edit/{userName}")
    public ModelAndView editUser(@PathVariable String userName, ModelMap model) {
        
        ModelAndView mav = new ModelAndView();

        // Model uz muze atribut "user" obsahovat a to v pripade, ze predchozi update/put daneho usera obsahoval chyby
        // a presmeroval na tento handler
        if (!model.containsAttribute("user")) {
            UserDataDTO user = userService.findByUserNameToTransfer(userName);
            mav.addObject("user", user);
        }
        
        mav.setViewName("user_registration");
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
     * ADMIN muze u jineho ADMINA editovat heslo a ROLES, krome ROLE_ADMIN.
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
    public String editUserAccount(@ModelAttribute("user") @Valid UserDataDTO userDto,
                                                                 BindingResult result,
                                                                 ModelMap model,
                                                                 HttpServletRequest request,
                                                                 RedirectAttributes attr) {
        
        // Neprihlaseny uzivatel muze byt editovany ADMINem - ten muze menit pouze Password a ROLES, pokud nejde taky o ADMIN usera.
        // ADMIN nemuze byt editovan jinym ADMINem.
        User loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser != null) { // Jde o prihlaseneho uzivatele - muze pokracovat editace
            // Prihlaseny uzivatel meni svoje data - je potreba overit userName a e-mail, ktere se nesmi shodovat s jinym jiz vytvorenym.
            // Pokud jineho usera edituje ADMIN, neni potreba overovat, protoze ADMIn nemuze menit ve formulari ani userName ani e-mail
            if (userDto.getId() == loggedInUser.getId()) { 
                // Tento blok je spravny pouze pokud prihlaseny user meni svoje udaje (je jedno jaky ma Profile)
                if (!loggedInUser.getUserName().equalsIgnoreCase(userDto.getUserName())) { // Prihlaseny uzivatel chce zmenit userName
                    // Je uzivatelske jmeno jiz pouzito
                    if (!userService.isUserNameUnique(userDto.getId(), userDto.getUserName())) {
                        result.rejectValue("userName", "error.user.name.used", "There is already an account registered with that user name.");
                    }
                }
                
                // Prihlaseny user chce zmenit svoji e-mail adresu - overit jestli je to mozne
                if (!loggedInUser.getEmail().equalsIgnoreCase(userDto.getEmail())) { // nova e-mail adresa se lisi nebo je smazana
                    
                    // Je e-mail jiz pouzit ?
                    if (!userService.isEmailUnique(userDto.getId(), userDto.getEmail())) {
                        result.rejectValue("email", "error.user.emailused", "There is already an account registered with that e-mail");
                    } 
                }
            }
            
            // Kontrola hesla. Muze byt prazdne. Pokud neni, musi se shodovat s confirmPassword
            if (!userDto.getPassword().isEmpty()
                && !userDto.getPassword().equals(userDto.getConfirmPassword()))
                    result.rejectValue("confirmPassword", "error.user.password.confirm", "Confirmation password does not match password.");
        }
        
        if (result.hasErrors()) { // In case of error, show the user edit page again with errors          
            // Pokud se maji predat bindingResult, ktery obsahuje chyby do dalsiho redirect View, musi se prenest timto zpusobem
            attr.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            attr.addFlashAttribute("user", userDto);
            return "redirect:/user/edit/" + userDto.getUserName();
        }
        
        boolean userModifySuccess = false;
        
        User updatedUser = userService.updateUser(userDto);
        
        if (updatedUser != null) {
            userModifySuccess = true;
            
            if (loggedInUser != null 
                && updatedUser.getId() == loggedInUser.getId()) { // If the user modifies it's own profile
                // Check if the email address is confirmed
                if (!updatedUser.isRegisterEmailConfirmed()
                     && !updatedUser.getEmail().isEmpty()) { // novy email nepotvrzen a neprazdny. Poslat confirm e-mail token
                    String appUrl = "http://" + request.getServerName() +  ":" + request.getServerPort() +  request.getContextPath();
                    tokenCreateAndSendEmailService.setUserVerificationData(updatedUser, appUrl, request.getLocale());
                    try {
                        tokenCreateAndSendEmailService.createAndSendVerificationTokenEmail();
                        attr.addFlashAttribute("verificationEmailSent", true); // requires processing of the "verificationEmailSent" attr. in "redirect:/user/edit/", see bellow
                    } catch(MailException e) {
                        logger.error("MailException: {}", e.getMessage());
                        
                        String sendEmailErrorMessage = messages.getMessage("register.verificationemail.sent.failure", null, request.getLocale());
                        attr.addFlashAttribute("sendVerificationEmailErrorMessage", sendEmailErrorMessage);
                    }
                }
            }
        }
        
        attr.addFlashAttribute("userName", updatedUser.getUserName());
        attr.addFlashAttribute("userModifySuccess", userModifySuccess);
        return "redirect:/user/edit/" + updatedUser.getUserName();
    }
    
    
    // ------------------- Retrieve Single User -------------------------------------------------------- //
      
    /**
     * Not used, yet.
     * 
     * @param id
     * @return
     */
    @GetMapping("/show/{id}")
    public ResponseEntity<UserDataDTO> getUser(@PathVariable("id") Integer id) {
        
        logger.info("Fetching User with id " + id);
        UserDataDTO user = userService.findByIdToTransfer(id);
        if (user == null) {
            logger.info("User with id " + id + " not found");
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
        return new ResponseEntity<UserDataDTO>(user, HttpStatus.OK);
    }
    
    // ------------------- Delete a User -------------------------------------------------------- //
      
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public String deleteUser(@PathVariable("id") Integer id) {
        
        UserDataDTO user = userService.findByIdToTransfer(id);
        if (user == null) {
            logger.info("Unable to delete. User with id " + id + " not found");
        } else
            userService.deleteUserById(id);
        
        return "redirect:/user/all";
    }
  
    @ModelAttribute("allUserProfiles")
    public List<UserProfile> populateUserProfiles() {
        return userProfileService.findAll();
    }
    
}