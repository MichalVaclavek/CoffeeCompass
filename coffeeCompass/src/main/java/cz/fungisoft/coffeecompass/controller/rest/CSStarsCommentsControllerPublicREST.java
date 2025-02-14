package cz.fungisoft.coffeecompass.controller.rest;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.StarsQualityDescriptionDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.StarsQualityService;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;

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
 */
@Tag(name = "Stars and comments", description = "Stars rating and comments of the coffee site")
@RestController
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/starsAndComments")
public class CSStarsCommentsControllerPublicREST {

    private final ICommentService commentsService;
    
    private final StarsQualityService starsQualityService;
    
    private final CoffeeSiteService coffeeSiteService;
    
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
     * @param commentExtId Comment to be returned
     * @return
     */
    @GetMapping("/comment/{commentExtId}")
    public ResponseEntity<CommentDTO> getCommentByID(@PathVariable String commentExtId) {
        CommentDTO comment = null;
        
        // Ulozit Comment if not empty
        if (commentExtId != null) {
            comment = commentsService.getByExtIdToTransfer(commentExtId);
        }
        
        return (comment != null) ? new ResponseEntity<>(comment, HttpStatus.OK)
                                 : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    
    /**
     * Zpracuje GET pozadavek vsechny comments (muze byt pouzito pro offline mode na clientu)
     * 
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
     * @return
     */
    @GetMapping("/comments/allPaginated") // https://localhost:8443/api/v1/coffeesites/starsAndComments/comments/allPaginated/?size=5&page=1
    @ResponseStatus(HttpStatus.OK)
    public Page<CommentDTO> getAllCommentsPaginated(@RequestParam(defaultValue = "created") String orderBy,
                                                    @RequestParam(defaultValue = "desc") String direction,
                                                    @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        
        int currentPage = page;
        int pageSize = size;
        Page<CommentDTO> allCommentsPage;
        
        // Get 1 page of all ACTIVE CoffeeSites
        allCommentsPage = commentsService.findAllCommentsPaginated(PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString(direction.toUpperCase()), orderBy)));
        
        
        // Gets all comments page with the given number of comments
        return  allCommentsPage;
        
    }

    /**
     * Returns all comments of the CoffeeSite of id=siteExtId.
     * <p>
     * It returns CommentDTO.canBeDeleted = false in case of REST.<br>
     * But the comment Delete request can be sent containing respective JWT<br>
     * of the loged-in user. Therefore, the client decides if the<br>
     * comment can be deleted based on valid JWT token it received<br>
     * during login of the user.
     * 
     * @param siteExtId id of coffeeSite who's comments are requested
     * @return
     */
    @GetMapping("/comments/{siteExtId}") // napr. https://localhost:8443/api/v1/coffeesites/starsAndComments/comments/165ab401-a043-4358-9f52-fb18b2961d27
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDTO> commentsBySiteId(@PathVariable("siteExtId") String siteExtId) {
        
        // Gets all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(siteExtId);
        
        if (comments == null) {
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", siteExtId);
        }
        
        return comments;
    }
    
    /**
     * Returns all comments of the CoffeeSite of id=siteId Paginated.
     * 
     * @param siteExtId id of coffeeSite who's comments are requested
     * @return
     */
    @GetMapping("/commentsPaginated/{siteExtId}/") // napr. https://localhost:8443/api/v1/coffeesites/starsAndComments/commentsPaginated/2/?size=5&page=1
    @ResponseStatus(HttpStatus.OK)
    public Page<CommentDTO> commentsBySiteIdPaginated(@PathVariable("siteExtId") String siteExtId,
                                                      @RequestParam(defaultValue = "created") String orderBy,
                                                      @RequestParam(defaultValue = "desc") String direction,
                                                      @RequestParam(name="page", defaultValue = "1") Integer page, @RequestParam(name="size", defaultValue = "10") Integer size) {
        int currentPage = page;
        int pageSize = size;

        return coffeeSiteService.findOneByExternalId(siteExtId)
                .map(coffeeSite -> commentsService.findAllCommentsForSitePaginated(coffeeSite, PageRequest.of(currentPage - 1, pageSize, Sort.by(Sort.Direction.fromString(direction.toUpperCase()), orderBy))))
                .orElseThrow(() -> new ResourceNotFoundException("Comments", "coffeeSiteId", siteExtId));
    }
    
    /**
     * Returns number of Comments created for this CoffeeSiteID.
     * 
     * @param siteExtId
     * @return
     */
    @GetMapping("/comments/number/{siteExtId}") // napr. http://localhost:8080/api/v1/coffeesites/starsAndComments/comments/number/165ab401-a043-4358-9f52-fb18b2961d27
    @ResponseStatus(HttpStatus.OK)
    public Integer numberOfCommentsForSiteId(@PathVariable("siteExtId") String siteExtId) {
        
        // Gets number of all comments for this coffeeSite
        Integer commentsNumber = commentsService.getNumberOfCommentsForSiteId(siteExtId);
        
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
    public List<StarsQualityDescriptionDTO> populateAllQualityStars() {
        return starsQualityService.getAllStarsQualityDescriptions();
    }
}
