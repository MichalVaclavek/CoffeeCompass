package cz.fungisoft.coffeecompass.controller.rest.secured;


import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.StarAndCommentForSiteModel;
import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.exceptions.rest.InvalidParameterValueException;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;

/**
 * REST Controller for handling addition/deletition/update of Comment and Stars for CoffeeSite.<br>
 * REST verze<br>
 * Obsluhuje operace souvisejici s vkladanim/mazanim/modifikaci hodnoceni a komentare ke CoffeeSitu.  
 * 
 * @author Michal Vaclavek
 *
 */
@Tag(name = "RatingAndComments", description = "Coffee site's rating and comments")
@RestController 
@RequestMapping("${site.coffeesites.baseurlpath.rest}" + "/secured/starsAndComments")
public class CSStarsCommentsControllerSecuredREST {
    
    private static final Logger LOG = LoggerFactory.getLogger(CSStarsCommentsControllerSecuredREST.class);
    
    private final ICommentService commentsService;
    
    private final IStarsForCoffeeSiteAndUserService starsForCoffeeSiteService;
    
    private final CoffeeSiteService coffeeSiteService;
    
    
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
     * Zpracuje POST pozadavek z REST clienta, ktery pozaduje ulozit Comment a Stars pro jeden CoffeeSite<br>
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
     * @param coffeeSiteExtId CoffeeSite id to which the StarsAndCommentModel belongs to
     * 
     * @return list of comments belonging to this coffeeSitie id
     */
    @PostMapping(value="/saveStarsAndComment/{coffeeSiteExtId}", headers = {"content-type=application/json"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommentDTO>> saveCommentAndStarsForSite(@RequestBody @Valid StarsAndCommentModel starsAndComment,
                                                                       @PathVariable("coffeeSiteExtId") String coffeeSiteExtId) {
        try {
            // Ulozit hodnoceni if not empty
            starsForCoffeeSiteService.saveStarsForCoffeeSiteAndLoggedInUser(coffeeSiteExtId, starsAndComment.getStars().getNumOfStars());
        } catch (Exception ex) { // can be resource not found exception in case of wrong coffeeSiteId
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", coffeeSiteExtId);
        }
        
        coffeeSiteService.findOneByExternalId(coffeeSiteExtId).ifPresent(cs -> {
            // Ulozit Comment for the CoffeeSite, if not empty
            if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty()) {
                commentsService.saveTextAsComment(starsAndComment.getComment(), cs);
            }
        });
        
        // Gets all comments for this coffeeSite
        List<CommentDTO> comments = commentsService.getAllCommentsForSiteId(coffeeSiteExtId);
        
        if (comments == null) {
            throw new ResourceNotFoundException("Comments", "coffeeSiteId", coffeeSiteExtId);
        }
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
    
    /**
     * Zpracuje POST pozadavek z REST clienta, ktery pozaduje ulozit vice novych Comments a Stars pro vice CoffeeSites<br>
     * <p>
     * Example of Correct JSON request body:
     *
     * [
     * { "stars":  2,
     *   "comment": "Docela dobra kava",
     *   "coffeeSiteId": 15
     * },
     * {"stars":  3,
     *   "comment": "Kavicka nam",
     *   "coffeeSiteId": 49
     *   }
     * ]
     * 
     * Returns true if there was no error during saving
     * 
     * @param starsAndComments list of StarAndCommentForSiteModel containing Comments and Stars rating to be saved
     * 
     * @return true if no error during saving
     */
    //@PostMapping(value="/saveStarsAndComment/{coffeeSiteExtId}", headers = {"content-type=application/json"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value="/saveStarsAndComments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> insertCommentsAndStarsForSites(@RequestBody @Valid
                                                                  List<StarAndCommentForSiteModel> starsAndComments,
                                                                  final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidParameterValueException("Comments_stars_rating", bindingResult.getFieldErrors());
        }
        LOG.debug("Saving comments and/or stars rating. Number of items to be saved: {}", starsAndComments.size());
        AtomicInteger savedComments = new AtomicInteger();
        // Ulozit vsechna hodnoceni if not empty
        for (StarAndCommentForSiteModel starsAndComment : starsAndComments) {
            String coffeeSiteExtId = starsAndComment.getCoffeeSiteId();
            starsForCoffeeSiteService.saveStarsForCoffeeSiteAndLoggedInUser(coffeeSiteExtId, starsAndComment.getStars());

            coffeeSiteService.findOneByExternalId(coffeeSiteExtId).ifPresent(cs -> {
                // Ulozit Comment for the CoffeeSite, if not empty
                if ((starsAndComment.getComment() != null) && !starsAndComment.getComment().isEmpty()) {
                    if (commentsService.saveTextAsComment(starsAndComment.getComment(), cs) != null) {
                        savedComments.getAndIncrement();
                    }
                }
            });
        }
        
        LOG.debug("Number of comments saved: {}", savedComments.get());
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
    
    
    /**
     * Zpracuje PUT pozadavek na update Commentu a Stars od daneho Usera pro dany Comment id.
     * <p>
     * Pro update jenom Stars lze take pouzit end pointy:<br>
     * /rest/secured/starsAndComments/updateStarsForCoffeeSiteAndUser/coffeeSite/{coffeeSiteId}/user/{userId}/stars/{numOfStars}
     *   JSON CommentDTO to update example:
     *   {
     *       "id": "06897c72-d75a-4272-95a1-74cb53fb29fb",
     *       "text": "sfsgsg sgsg",
     *       "created": "30.03. 2025 23:28",
     *       "coffeeSiteID": 204,
     *       "userId": "93cc3e8f-7569-4a85-a04e-ee29f94b2bf5",
     *       "starsFromUser": 4
     *   }
     * 
     * @param commentDTO Comment to be updated. Muze obsahovat prazdny text, pak je komentar smazan a odeslan status HttpStatus.NOT_FOUND
     * 
     * @return updatovany Comment nebo indikace chyby na vstupu (HttpStatus.BAD_REQUEST) nebo HttpStatus.NOT_FOUND,
     *         pokud byl Comment smazan nebo nebyl nalezen.
     */
    @PutMapping("/updateCommentAndStars")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommentDTO> updateCommentAndStarsForSite(@RequestBody CommentDTO commentDTO) {
        if (commentDTO != null) {
            // Ulozit updated Stars first, if not null
            starsForCoffeeSiteService.updateStarsForCoffeeSiteAndUser(commentDTO);
            
            Comment updatedComment = null;
            CommentDTO commentToReturn = null;
            try {
                updatedComment = commentsService.updateComment(commentDTO);
                
                if (updatedComment != null) {
                    commentToReturn = commentsService.getByExtIdToTransfer(updatedComment.getId().toString());
                    LOG.info("Comment updated for CoffeeSite id {}, from User id {}.", commentDTO.getCoffeeSiteId(), commentDTO.getUserId());
                }
            } catch (Exception ex) {
                LOG.error("Error calling update Comment service.", ex);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return (commentToReturn != null) ? new ResponseEntity<>(commentToReturn, HttpStatus.OK)
                                             : new ResponseEntity<>(HttpStatus.NOT_FOUND); // means not found or deleted
                    
        }
        
        LOG.error("Error update Comment, input validation failed.");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    
    
    /**
     * Zpracuje PUT pozadavek na update vice Commentu a Stars od danych useru pro vice CoffeeSites
     * <p>
     * 
     *   JSON  List<CommentDTO> to update example:
     *   [
            {
                "id": 189,
                "text": "FDFDFDF fdfdfg",
                "created": "05.03. 2021 19:47:10",
                "coffeeSiteID": 15,
                "userName": "google1",
                "userId": 63,
                "canBeDeleted": false,
                "starsFromUser": 4
            },
            {
                "id": 190,
                "text": "Nazdar, update 222",
                "created": "01.02. 2021 13:18",
                "coffeeSiteID": 49,
                "userName": "google1",
                "userId": 63,
                "canBeDeleted": false,
                "starsFromUser": 3
            }
        ]
     * 
     * @param comments - list of Comments and Stars rating to be updated. Muze obsahovat prazdny text, pak je komentar smazan.
     * 
     * @return true, if updated with no problem
     */
    @PutMapping("/updateCommentsAndStars")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Boolean> updateCommentsAndStarsForSites(@RequestBody List<CommentDTO> comments) {
        final int[] updatedComments = {0}; // to be final in lambda of the forEach
        if (comments != null) {
            LOG.info("Updating comments and/or stars rating. Number of items to be updated: {}", comments.size());
            comments.stream().filter(Objects::nonNull)
                             .forEach(comment -> {
                                      starsForCoffeeSiteService.updateStarsForCoffeeSiteAndUser(comment);
                                      
                                      Comment updatedComment = null;
                                      try {
                                          updatedComment = commentsService.updateComment(comment);
                                          if (updatedComment != null) {
                                              updatedComments[0]++;
                                              LOG.debug("Comment updated for CoffeeSite id {}, from User id {}.", comment.getCoffeeSiteId(), comment.getUserId());
                                          }
                                      } catch (Exception ex) {
                                          LOG.error("Error calling update Comment service.", ex);
                                          //throw new RESTException("Error updating comment."); // do not throw exception here, next comment save can be be fine
                                      }
                                   });
                    
        }
        LOG.info("Comments with stars rating update finished. Number of comments updated: {}", updatedComments[0]);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
    
    
    
    /**
     * Zpracuje PUT pozadavek na update hodnoceni Stars pro dane CoffeeSite id a daneho User id
     * 
     * @param numOfStars
     * @param coffeeSiteExtId
     * @param userExtId
     * 
     * @return 
     */
    @PutMapping("/updateStarsForCoffeeSiteAndUser/coffeeSite/{coffeeSiteExtId}/user/{userExtId}/stars/{numOfStars}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Integer> updateStarsForCoffeeSiteAndUser(@PathVariable(value="numOfStars") int numOfStars,
                                                                   @PathVariable(value="coffeeSiteExtId") String coffeeSiteExtId,
                                                                   @PathVariable(value="userExtId") String userExtId) {
        // Save updated Stars if not null
        if (numOfStars >= StarsQualityDescription.StarsQualityEnum.ONE.ordinal() + 1
               && numOfStars <= StarsQualityDescription.StarsQualityEnum.FIVE.ordinal() + 1) {
            StarsForCoffeeSiteAndUser starsForCoffeeSiteAndUser = starsForCoffeeSiteService.updateStarsForCoffeeSiteAndUser(coffeeSiteExtId, userExtId, numOfStars);
            
            return (starsForCoffeeSiteAndUser != null) ? new ResponseEntity<>(starsForCoffeeSiteAndUser.getStars().getNumOfStars(), HttpStatus.OK)
                                                       : new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
        }
        
        return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM nebo prihlasenym autorem hodnoceni s roli USER.<br>
     * Vrati aktualni pocet Comments pro coffee site, ke kteremu patril smazany komentar.<br>
     * 
     * @param commentExtId id of the Comment to be deleted
     * @return number of Comments of the coffeeSite where the deleted comment belonged to
     */
    @DeleteMapping("/deleteComment/{commentExtId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Integer> deleteCommentAndStarsForSite(@PathVariable("commentExtId") String commentExtId) {
        
        UUID siteId;
        siteId = commentsService.deleteCommentByExtId(commentExtId);
        
        if (siteId == null) {
            throw new ResourceNotFoundException("Comment", "commentId", commentExtId);
        }
        Integer commentsNumber = commentsService.getNumberOfCommentsForSiteId(siteId);
        
        if (commentsNumber == null) {
            commentsNumber = 0;
        }
            
        return new ResponseEntity<>(commentsNumber, HttpStatus.OK);
    }
}
