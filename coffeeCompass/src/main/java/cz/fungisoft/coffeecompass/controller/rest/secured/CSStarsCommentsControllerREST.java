package cz.fungisoft.coffeecompass.controller.rest.secured;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import io.swagger.annotations.Api;

/**
 * Controller for handling addition/deletition of Comment and Stars for CoffeeSite.<br>
 * REST verze<br>
 * Obsluhuje operace souvisejici s vkladanim/mazanim hodnoceni a komentare ke CoffeeSitu  
 * <br>
 * Pro ziskani techto informaci ke CoffeeSitu se pouziva Controler CoffeeSiteController,
 * ktery vola prislusne Service, ktere dodaji Comments a prumerne hodnoceni k danemu
 * CoffeeSitu
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
     * Zpracuje POST pozadavek z REST consumera, ktery pozaduje ulozit Comment a Stars pro jeden CoffeeSite<br>
     * 
     * NOT YET USED in REST CONTROLLER
     * 
     * @param starsAndComment
     * @param id CoffeeSite id to which the StarsAndCommentModel belongs to
     * @return
     */
    @PostMapping("/saveStarsAndComment/{coffeeSiteId}") 
    public ResponseEntity<Void> saveCommentAndStarsForSite(@ModelAttribute StarsAndCommentModel starsAndComment, @PathVariable Long coffeeSiteId) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSite(coffeeSiteId, starsAndComment.getStars().getNumOfStars());
        
        CoffeeSite cs = coffeeSiteService.findOneById(coffeeSiteId);
        
        if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty())
            commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
        
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM ... <br>
     * 
     * NOT YET USED in REST CONTROLLER
     * 
     * @param id of the Comment to delete
     * @return
     */
    @DeleteMapping("/deleteComment/{commentId}") 
    public ResponseEntity<Void> deleteCommentAndStarsForSite(@PathVariable Integer commentId) {
        // Smazat komentar - need to have site Id to give it to /showSite Controller
        Long siteId = commentsService.deleteCommentById(commentId);
        
        if (siteId != null) {
           return new ResponseEntity<Void>(HttpStatus.OK);
        } else {
            return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
        }
    }

}
