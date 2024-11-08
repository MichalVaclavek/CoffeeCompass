package cz.fungisoft.coffeecompass.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.controller.models.AuthProviders;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.StatisticsToShow;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.StatisticsInfoService;

/**
 * Controller pro obsluhu zakladnich odkazu, prevazne z home.html stranky.<br>
 * Obsluhuje i zakladni chybove odkazy, jako /403, /404 a /500
 * <p>
 * Vrací základní html stránky uložené v src/main/resources/templates
 * 
 * @author Michal Vaclavek
 */
@Controller
public class BasicController {

    private final StatisticsInfoService statsService;
    
    private final CoffeeSiteService coffeeSiteService;
    
    private final ClientRegistrationRepository clientRegistrationRepository;
    
    private static final String OAUTH2_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorize";
    private final Map<String, String> oauth2AuthenticationUrls = new HashMap<>();
    
    
    @Autowired
    public BasicController(StatisticsInfoService statsService,
                           CoffeeSiteService coffeeSiteService,
                           ClientRegistrationRepository clientRegistrationRepository) {
        super();
        this.statsService = statsService;
        this.coffeeSiteService = coffeeSiteService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }


    @GetMapping(value= {"/home", "/home/", "/"})
    public ModelAndView home() {
        // Gets and shows statistical info
        ModelAndView mav = new ModelAndView("home");
        StatisticsToShow stats = statsService.getCurrentStatisticalInfoToShow();
        mav.addObject("stats", stats);
        // Gets and shows latest created and ACTIVE CoffeeSites
        List<CoffeeSiteDTO> latestSites = coffeeSiteService.getLatestCoffeeSites(4); 
        mav.addObject("latestSites", latestSites);
        
        return mav;
    }
    
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/login")
    public String login(Model model) {
        // Enter Social login links/buttons to login page/form
        Iterable<ClientRegistration> clientRegistrations = null;
        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
        if (type != ResolvableType.NONE && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        }
   
        clientRegistrations.forEach(registration ->  oauth2AuthenticationUrls.put(registration.getClientName().toLowerCase(), 
                                    OAUTH2_AUTHORIZATION_REQUEST_BASE_URI + "/" + registration.getRegistrationId()));
        
        model.addAttribute("oAuth2RegUrlGoogle", oauth2AuthenticationUrls.get(AuthProviders.GOOGLE.toString().toLowerCase()));
        model.addAttribute("oAuth2RegUrlFacebook", oauth2AuthenticationUrls.get(AuthProviders.FACEBOOK.toString().toLowerCase()));
        
        return "login";
    }
    
    @GetMapping("/logout")
    public String logout() {
        return "login";
    }
    
    /**
     * Stranka pro zobrazeni udaju o ochrane soukromi
     * @return
     */
    @GetMapping("/privacypolicy")
    public String privacyPolicy() {
        return "privacy_policy";
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
