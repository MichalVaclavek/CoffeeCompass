package cz.fungisoft.coffeecompass.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller pro obsluhu zakladnich odkazu.
 * <br>
 * Vrací základní html stránky uložené v src/main/resources/templates
 * 
 * @author Michal Vaclavek
 */
@Controller
public class BasicController
{   
    @GetMapping("/")
    public String home1() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "login";
    }
    
    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }
    
    @GetMapping("/404")
    public String error404() {
        return "error/404";
    }
    
    @GetMapping("/500")
    public String error500() {
        return "error/500";
    }

}
