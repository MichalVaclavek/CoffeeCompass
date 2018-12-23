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
 * Obsluhuje operace souvisejici s hondocenim CoffeeSitu a s vkladanim/mazanim
 * komentare ke CoffeeSitu na strance/templatu coffeesite_info.html
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@Controller 
public class CSStarsRatingAndCommentsController
{
    private ICommentService commentsService;
    
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    private CoffeeSiteService coffeeSiteService;
    
    
    @Autowired
    public CSStarsRatingAndCommentsController(ICommentService commentsService, IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService,
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
    @PostMapping("/saveStarsAndComment/{coffeeSiteId}") 
    public ModelAndView saveCommentAndStarsForSite(@ModelAttribute StarsAndCommentModel starsAndComment, @PathVariable Long coffeeSiteId) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSite(coffeeSiteId, starsAndComment.getStars().getNumOfStars());
        
        CoffeeSite cs = coffeeSiteService.findOneById(coffeeSiteId);
        
        if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty())
            commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
        
        // Show same coffee site with new Stars and comments
        ModelAndView mav = new ModelAndView("redirect:/showSite/"+ coffeeSiteId);
        
        return mav;
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare ze stranky zobrazujici komentare k jednomu CoffeeSitu.
     * Muze byt volano pouze ADMINEM (zarizeno v Thymeleaf View strance coffeesite_detail.html)
     * 
     * @param id of the Comment to delete
     * @return
     */
    @DeleteMapping("/deleteComment/{commentId}") 
    public ModelAndView deleteCommentAndStarsForSite(@PathVariable Integer commentId) {
        // Smazat komentar - need to have site Id to give it to /showSite Controller
        Long siteId = commentsService.deleteCommentById(commentId);
        
        // Show same coffee site with updated Stars and comments
        ModelAndView mav = new ModelAndView("redirect:/showSite/"+ siteId);
        
        return mav;
    }

}
