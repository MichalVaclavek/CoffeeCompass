package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
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
@RestController 
@RequestMapping("/rest/public/starsAndComments")
public class CSStarsCommentsControllerPublicREST
{
    private ICommentService commentsService;
    
    private StarsQualityService starsQualityService;
    
    @Autowired
    public CSStarsCommentsControllerPublicREST(ICommentService commentsService,
                                               StarsQualityService starsQualityService) {
        super();
        this.commentsService = commentsService;
        this.starsQualityService = starsQualityService;
    }

    /**
     * Returns all comments of the CoffeeSite of id=siteId.
     * <p>
     * It returns CommentDTO.canBeDeleted = false in case of REST.<br>
     * But the comment Delete request can be sent containing respective JWT<br>
     * of the loged-in user. Therefore, the client decides if the<br>
     * comment can be deleted based on valid JWT token it received<br>
     * during login of the user.
     * 
     * @param siteId id of coffeeSite who's comments are requested
     * @return
     */
    @GetMapping("/comments/{siteId}") // napr. http://localhost:8080/rest/public/starsAndComments/comments/2
    public ResponseEntity<List<CommentDTO>> commentsBySiteId(@PathVariable("siteId") Long siteId) {
        
        // Gets all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(siteId);
        
//        return (comments == null) ? new ResponseEntity<List<CommentDTO>>(HttpStatus.NOT_FOUND)
//                                  : new ResponseEntity<List<CommentDTO>>(comments, HttpStatus.OK);
        if (comments == null) {
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", siteId);
        }
        
        return new ResponseEntity<List<CommentDTO>>(comments, HttpStatus.OK);
    }
    
    /**
     * A method, which gets all valid/possible slovni hodnoceni<br>
     * kvality kavy to be selected by user during hodnoceni.<br>
     * Used as an options on form to rate coffee site's coffee quality.
     * 
     * @return
     */
    @GetMapping("/starsQualityDescription/all")
    public ResponseEntity<List<StarsQualityDescription>> populateAllQualityStars() {
        
        List<StarsQualityDescription> starsQuality = starsQualityService.getAllStarsQualityDescriptions();
        return new ResponseEntity<List<StarsQualityDescription>>(starsQuality,  HttpStatus.OK);
    }

}