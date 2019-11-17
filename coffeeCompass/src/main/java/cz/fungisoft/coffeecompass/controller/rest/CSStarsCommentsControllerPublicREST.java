package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
public class CSStarsCommentsControllerPublicREST
{
    private ICommentService commentsService;
    
    @Autowired
    public CSStarsCommentsControllerPublicREST(ICommentService commentsService, IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService,
                                CoffeeSiteService coffeeSiteService) {
        super();
        this.commentsService = commentsService;
    }

    /**
     * Returns all comments of the CoffeeSite of id=siteId
     * 
     * @param siteId
     * @return
     */
    @GetMapping("/comments/{siteId}") // napr. http://localhost:8080/rest/starsAndComments/comments/2
    public ResponseEntity<List<CommentDTO>> commentsBySiteId(@PathVariable Long siteId) {
        
        // Add all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(siteId);
        
        if (comments == null) {
            return new ResponseEntity<List<CommentDTO>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<CommentDTO>>(comments, HttpStatus.OK);
    }

}
