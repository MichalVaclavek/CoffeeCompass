package cz.fungisoft.coffeecompass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import io.swagger.annotations.Api;

/**
 * Controller for handling addition/deletition of Comment and Stars for CoffeeSite.
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@Controller 
public class CSCommentsController
{
    private ICommentService commentsService;
    
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    private CoffeeSiteService coffeeSiteService;
    
    
    @Autowired
    public CSCommentsController(ICommentService commentsService, IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService,
                                CoffeeSiteService coffeeSiteService) {
        super();
        this.commentsService = commentsService;
        this.starsForCoffeeSiteService = starsForCoffeeSiteService;
        this.coffeeSiteService = coffeeSiteService;
    }


    /**
     * Zpracuje POST pozadavek ze stranky zobrazujici info o jednom CoffeeSite, z Formu, ktery umoznuje zadat
     * hodnoceni a komentar.
     * 
     * @param starsAndComment
     * @param id CoffeeSite id to which the StarsAndCommentModel belongs to
     * @return
     */
    @PostMapping("/saveStarsAndComment/{id}") 
    public ModelAndView saveCommentAndStarsForSite(@ModelAttribute StarsAndCommentModel starsAndComment, @PathVariable Integer id) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSite(id, starsAndComment.getStars().getNumOfStars());
        
        CoffeeSite cs = coffeeSiteService.findOneById(id);
        
        if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty())
            commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
        
        // Show same coffee site with new Stars and comments
        ModelAndView mav = new ModelAndView("redirect:/showSite/"+ id);
        
        return mav;
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare ze stranky zobrazujici komentare k jednomu CoffeeSitu.
     * Muze byt volano pouze ADMINEM (zarizeno v Thymeleaf View strance coffeesite_detail.html)
     * 
     * @param id of the Comment to delete
     * @return
     */
    @DeleteMapping("/deleteComment/{id}") 
    public ModelAndView deleteCommentAndStarsForSite(@PathVariable Integer id) {
        // Smazat komentar - need to have site Id to give it to /showSite Controller
        Integer siteId = commentsService.deleteCommentById(id);
        
        // Show same coffee site with updated Stars and comments
        ModelAndView mav = new ModelAndView("redirect:/showSite/"+ siteId);
        
        return mav;
    }

}
