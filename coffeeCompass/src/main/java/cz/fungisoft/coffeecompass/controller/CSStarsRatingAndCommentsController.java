package cz.fungisoft.coffeecompass.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Controller for handling addition/deletition/update of Comment and Stars for CoffeeSite.<br>
 * Obsluhuje operace souvisejici s vkladanim/mazanim/editaci hodnoceni a komentare ke CoffeeSitu<br>  
 * na strance/templatu coffeesite_info.html<br>
 * <br>
 * Pro ziskani techto informaci ke CoffeeSitu se pouziva Controler CoffeeSiteController,<br>
 * ktery vola prislusne Service, ktere dodaji Comments a prumerne hodnoceni k danemu CoffeeSitu
 * 
 * @author Michal Vaclavek
 *
 */
@Controller 
public class CSStarsRatingAndCommentsController {

    private final ICommentService commentsService;
    
    private final IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    private final CoffeeSiteService coffeeSiteService;
    
    
    @Autowired
    public CSStarsRatingAndCommentsController(ICommentService commentsService,
                                              IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService,
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
     * @param coffeeSiteId id of the CoffeeSite to which the StarsAndCommentModel belongs to
     * @return
     */
    @PostMapping("/saveStarsAndComment/{coffeeSiteId}") 
    public ModelAndView saveCommentAndStarsForSite(@ModelAttribute StarsAndCommentModel starsAndComment, @PathVariable Long coffeeSiteId) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSiteAndLoggedInUser(coffeeSiteId, starsAndComment.getStars().getNumOfStars());

        ModelAndView mavRet = new ModelAndView("404");
        return coffeeSiteService.findOneById(coffeeSiteId).map(cs -> {
            // Ulozit Comment if not empty
            if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty()) {
                commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
            }
            mavRet.setViewName("redirect:/showSite/" + coffeeSiteId);
            return mavRet;
        }).orElse(mavRet);
    }
    
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare ze stranky zobrazujici komentare k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM nebo autorem komentare (zarizeno v Thymeleaf View strance coffeesite_detail.html,<br>
     * ktery zobrazi delete tlacitko jen pokud jsou tyto podminky splneny). Zda muze byt Comment smazan se nastavi<br>
     * v Service vrstve CommentService
     * 
     * @param commentId id of the Comment to delete
     * @return
     */
    @DeleteMapping("/deleteComment/{commentId}") 
    public ModelAndView deleteCommentAndStarsForSite(@PathVariable Integer commentId) {
        // Smazat komentar - need to have site Id to give it to /showSite Controller
        Long siteId = commentsService.deleteCommentById(commentId);
        // Show same coffee site with updated Stars and comments
        return new ModelAndView("redirect:/showSite/" + siteId);
    }
}
