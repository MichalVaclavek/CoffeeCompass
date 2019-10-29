/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.service.UserService;

/**
 * Trida, ktera vsem Controlerum prida do  Modelu objekt prihlaseneho uzivatele.
 * Bude k dispozici na vsech stránkách. Je potřeba pro header a footer pro případ, že je přihlášený uživatel.
 * 
 * @author Michal Vaclavek
 */
@ControllerAdvice
public class LoggedInUserAdvice
{    
    private UserService userService;
    
    @Autowired
    public LoggedInUserAdvice(UserService userService) {
        super();
        this.userService = userService;
    }

    @ModelAttribute("loggedInUser")
    public UserDTO currentUser() {
        Optional<UserDTO> loggedInUser = userService.getCurrentLoggedInUserDTO();
        return loggedInUser.orElse(null);
    }
    
}