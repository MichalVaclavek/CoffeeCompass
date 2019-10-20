/**
 * 
 */
package cz.fungisoft.coffeecompass.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.service.UserSecurityService;
import cz.fungisoft.coffeecompass.service.UserService;

/**
 * Trida, ktera vsem Controlerum prida do  Modelu objekt prihlaseneho uzivatele.
 * Bude k dispozici na vsech stránkách. Je potřeba pro header a footer pro případ, že je přihlášený uživatel.
 * 
 * @author Michal Vaclavek
 */
@ControllerAdvice
public class UserAdvice
{    
    private UserService userService;
    
    @Autowired
    public UserAdvice(UserService userService) {
        super();
        this.userService = userService;
    }

    @ModelAttribute("loggedInUser")
    public User currentUser() {
        
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        return loggedInUser.orElse(null);
    }
    
}