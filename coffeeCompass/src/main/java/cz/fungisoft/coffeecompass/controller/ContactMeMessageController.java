package cz.fungisoft.coffeecompass.controller;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.dto.ContactMeMessageDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.service.IContactMeMessageService;
import cz.fungisoft.coffeecompass.service.UserService;

/**
 * Controller pro obsluhu formulare pro zadani Contact me zpravy.
 * <br>
 * 
 * @author Michal Vaclavek
 */
@Controller
public class ContactMeMessageController
{
    private IContactMeMessageService contactMeService;
    
    private UserService userService;
    
    @Autowired
    public ContactMeMessageController(IContactMeMessageService contactMeService, UserService userService) {
        super();
        this.contactMeService = contactMeService;
        this.userService = userService;
    }


    @GetMapping("/contactMe") 
    public ModelAndView getForm(final ContactMeMessageDTO cmMessage) {
   
        ModelAndView mav = new ModelAndView();
        
        User loggedInUser = userService.getCurrentLoggedInUser();
        
        if (loggedInUser != null) {
            cmMessage.setAuthorName(loggedInUser.getUserName());
            cmMessage.setEmail(loggedInUser.getEmail());
        }
        
        mav.addObject("contactMeMessage", cmMessage);
        mav.setViewName("contact_me_form");
        return mav;
    }
    
    @PostMapping("/contactMe") // Mapovani http POST na DB SAVE
    public String saveAndSendContactMeMessage(@ModelAttribute("contactMeMessage") @Valid ContactMeMessageDTO cmMessage, final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "contact_me_form";
        }
        
        contactMeService.saveContactMeMessage(cmMessage);
        return "redirect:/home/?contactmesuccess";
    }
    
}
