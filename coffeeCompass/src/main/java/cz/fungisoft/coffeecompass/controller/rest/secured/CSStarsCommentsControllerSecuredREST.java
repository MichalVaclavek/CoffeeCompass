package cz.fungisoft.coffeecompass.controller.rest.secured;


import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import io.swagger.annotations.Api;

/**
 * Controller for handling addition/deletition of Comment and Stars for CoffeeSite.<br>
 * REST verze<br>
 * Obsluhuje operace souvisejici s vkladanim/mazanim hodnoceni a komentare ke CoffeeSitu  
 * <br>
 * Pro ziskani techto informaci ke CoffeeSitu se pouziva Controler CoffeeSiteController,<br>
 * ktery vola prislusne Service, ktere dodaji Comments a prumerne hodnoceni k danemu<br>
 * CoffeeSitu.
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Anotace Swagger
@RestController // Ulehcuje zpracovani HTTP/JSON pozadavku z clienta a automaticky vytvari i HTTP/JSON response odpovedi na HTTP/JSON requesty
@RequestMapping("/rest/secured/starsAndComments")
public class CSStarsCommentsControllerSecuredREST
{
    private ICommentService commentsService;
    
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    private CoffeeSiteService coffeeSiteService;
    
    
    @Autowired
    public CSStarsCommentsControllerSecuredREST(ICommentService commentsService,
                                                IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService,
                                                CoffeeSiteService coffeeSiteService) {
        super();
        this.commentsService = commentsService;
        this.starsForCoffeeSiteService = starsForCoffeeSiteService;
        this.coffeeSiteService = coffeeSiteService;
    }


    /**
     * Zpracuje POST pozadavek z REST consumera, ktery pozaduje ulozit Comment a Stars pro jeden CoffeeSite<br>
     * Example of Correct JSON request body:
     * { "stars": {
     *             "numOfStars": 2
     *            },
     *   "comment": "Docela dobra kava"
     * }
     * 
     * Returns list of comments belonging to this coffeeSite id
     * 
     * @param starsAndComment
     * @param id CoffeeSite id to which the StarsAndCommentModel belongs to
     * @return list of comments belonging to this coffeeSitie id
     */
    //@PostMapping(value="/saveStarsAndComment/{coffeeSiteId}", headers = {"content-type=application/json"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value="/saveStarsAndComment/{coffeeSiteId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommentDTO>> saveCommentAndStarsForSite(@RequestBody @Valid StarsAndCommentModel starsAndComment,
                                                                       @PathVariable("coffeeSiteId") Long coffeeSiteId) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSite(coffeeSiteId, starsAndComment.getStars().getNumOfStars());
        
        CoffeeSite cs = coffeeSiteService.findOneById(coffeeSiteId);
        
        // Ulozit Comment for the CoffeeSite, if not empty
        if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty()) {
            commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
        }
        
        // Gets all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(coffeeSiteId);
        
        if (comments == null) {
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", coffeeSiteId);
        }
        return new ResponseEntity<List<CommentDTO>>(comments, HttpStatus.OK);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM nebo prihlasenym autorem hodnoceni s roli USER.<br>
     * Vrati siteID coffee situ, ke kteremu smazanuy komentar patril.<br>
     * 
     * @param id of the Comment to be deleted
     * @return siteID of the coffeeSite where the deleted comment belonged to
     */
    @DeleteMapping("/deleteComment/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Long> deleteCommentAndStarsForSite(@PathVariable("commentId") Integer commentId) {
        
        Long siteId = null;
        siteId = commentsService.deleteCommentById(commentId);
        
        if (siteId == null) {
            throw new ResourceNotFoundException("Comment", "commentId", commentId);
        }
         
        return new ResponseEntity<Long>(siteId, HttpStatus.OK);
            
    }

}
