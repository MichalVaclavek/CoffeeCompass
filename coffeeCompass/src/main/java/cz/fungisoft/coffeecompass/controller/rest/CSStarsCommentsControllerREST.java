package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import io.swagger.annotations.Api;

/**
 * Controller for handling addition/deletition of Comment and Stars for CoffeeSite.<br>
 * REST verze<br>
 * Obsluhuje operace souvisejici s hondocenim CoffeeSitu a s vkladanim/mazanim
 * komentare ke CoffeeSitu na strance/templatu coffeesite_info.html
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/starsAndComments")
public class CSStarsCommentsControllerREST
{
    private ICommentService commentsService;
    
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    private CoffeeSiteService coffeeSiteService;
    
    
    @Autowired
    public CSStarsCommentsControllerREST(ICommentService commentsService, IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService,
                                CoffeeSiteService coffeeSiteService) {
        super();
        this.commentsService = commentsService;
        this.starsForCoffeeSiteService = starsForCoffeeSiteService;
        this.coffeeSiteService = coffeeSiteService;
    }


    /**
     * Zpracuje POST pozadavek ze stranky zobrazujici info o jednom CoffeeSite, z Formu, ktery umoznuje zadat
     * hodnoceni a komentar.<br>
     * 
     * NOT YET USED in REST CONTROLLER
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
     * Returns all comments of the CoffeeSite of id=siteId
     * 
     * @param siteId
     * @return
     */
    @GetMapping("/{siteId}") // napr. http://localhost:8080//rest/starsAndComments/2
    public ResponseEntity<List<CommentDTO>> commentsBySiteId(@PathVariable Long siteId) {
        
        // Add all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(siteId);
        
        if (comments == null) {
            return new ResponseEntity<List<CommentDTO>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<CommentDTO>>(comments, HttpStatus.OK);
    }
    
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare ze stranky zobrazujici komentare k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM (zarizeno v Thymeleaf View strance coffeesite_detail.html)<br>
     * 
     * NOT YET USED in REST CONTROLLER
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
