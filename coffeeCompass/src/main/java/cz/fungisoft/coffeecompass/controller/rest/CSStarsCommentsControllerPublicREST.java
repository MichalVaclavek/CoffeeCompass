package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
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
@Api // Swagger
@RestController 
@RequestMapping("/rest/public/starsAndComments")
public class CSStarsCommentsControllerPublicREST
{
    private ICommentService commentsService;
    
    private StarsQualityService starsQualityService;
    
    private CoffeeSiteService coffeeSiteService;
    
    @Autowired
    public CSStarsCommentsControllerPublicREST(ICommentService commentsService,
                                               StarsQualityService starsQualityService,
                                               CoffeeSiteService coffeeSiteService) {
        super();
        this.commentsService = commentsService;
        this.starsQualityService = starsQualityService;
        this.coffeeSiteService = coffeeSiteService;
    }
    
    
    /**
     * Zpracuje GET pozadavek na Comment urciteho ID
     * 
     * @param comment Comment to be returned
     * @return
     */
    @GetMapping("/getComment/{commentId}") 
    public ResponseEntity<CommentDTO> getCommentByID(@PathVariable Integer commentId) {
        
        CommentDTO comment = null;
        
        // Ulozit Comment if not empty
        if (commentId != null) {
            comment = commentsService.getByIdToTransfer(commentId);
        }
        
        return (comment != null) ? new ResponseEntity<>(comment, HttpStatus.OK)
                                 : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    
    /**
     * Zpracuje GET pozadavek vsechny comments (muze byt pouzito pro offline mode na clientu)
     * 
     * @param comment Comment to be returned
     * @return
     */
    @GetMapping("/comments/all")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDTO> getAllComments() {
        // Gets all comments 
        return  commentsService.getAllComments();
        
    }
    
    /**
     * Zpracuje GET pozadavek vsechny comments (muze byt pouzito pro offline mode na clientu)
     * 
     * @param comment Comment to be returned
     * @return
     */
    @GetMapping("/comments/allPaginated") // https://localhost:8443/rest/public/starsAndComments/comments/allPaginated/?size=5&page=1
    @ResponseStatus(HttpStatus.OK)
    public Page<CommentDTO> getAllCommentsPaginated(@RequestParam(defaultValue = "created") String orderBy,
                                                    @RequestParam(defaultValue = "desc") String direction,
                                                    @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<CommentDTO> allCommentsPage;
        
        // Get 1 page of all ACTIVE CoffeeSites
        allCommentsPage = commentsService.findAllCommentsPaginated(PageRequest.of(currentPage - 1, pageSize, new Sort(Sort.Direction.fromString(direction.toUpperCase()), orderBy)));
        
        
        // Gets all comments page with the given number of comments
        return  allCommentsPage;
        
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
    @GetMapping("/comments/{siteId}") // napr. https://localhost:8443/rest/public/starsAndComments/comments/2
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDTO> commentsBySiteId(@PathVariable("siteId") Long siteId) {
        
        // Gets all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(siteId);
        
        if (comments == null) {
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", siteId);
        }
        
        return comments;
    }
    
    /**
     * Returns all comments of the CoffeeSite of id=siteId Paginated.
     * 
     * @param siteId id of coffeeSite who's comments are requested
     * @return
     */
    @GetMapping("/commentsPaginated/{siteId}/") // napr. https://localhost:8443/rest/public/starsAndComments/commentsPaginated/2/?size=5&page=1
    @ResponseStatus(HttpStatus.OK)
    public Page<CommentDTO> commentsBySiteIdPaginated(@PathVariable("siteId") Long siteId,
                                                                      @RequestParam(defaultValue = "created") String orderBy,
                                                                      @RequestParam(defaultValue = "desc") String direction,
                                                                      @RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
        
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<CommentDTO> commentsPage;
        
        CoffeeSite coffeeSite = coffeeSiteService.findOneById(siteId);
        
        if (coffeeSite == null) {
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", siteId);
        }
        
        // Get 1 page of Comments beloning to the given CoffeeSite id
        commentsPage = commentsService.findAllCommentsForSitePaginated(coffeeSite, PageRequest.of(currentPage - 1, pageSize, new Sort(Sort.Direction.fromString(direction.toUpperCase()), orderBy)));
        
        return commentsPage;
    }
    
    /**
     * Returns number of Comments created for this CoffeeSiteID.
     * 
     * @param siteId
     * @return
     */
    @GetMapping("/comments/number/{siteId}") // napr. http://localhost:8080/rest/public/starsAndComments/comments/number/2
    @ResponseStatus(HttpStatus.OK)
    public Integer numberOfCommentsForSiteId(@PathVariable("siteId") Long siteId) {
        
        // Gets number of all comments for this coffeeSite
        Integer commentsNumber = commentsService.getNumberOfCommentsForSiteId(siteId);
        
        if (commentsNumber == null) {
            commentsNumber = 0;
        }
        
        return commentsNumber;
    }
    
    /**
     * A method, which gets all valid/possible slovni hodnoceni<br>
     * kvality kavy to be selected by user during hodnoceni.<br>
     * Used as an options on form to rate coffee site's coffee quality.
     * 
     * @return
     */
    @GetMapping("/starsQualityDescription/all")
    @ResponseStatus(HttpStatus.OK)
    public List<StarsQualityDescription> populateAllQualityStars() {
        return starsQualityService.getAllStarsQualityDescriptions();
    }
}
