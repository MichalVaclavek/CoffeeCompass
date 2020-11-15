package cz.fungisoft.coffeecompass.controller.rest.secured;


import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cz.fungisoft.coffeecompass.controller.models.StarsAndCommentModel;
import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.StarsForCoffeeSiteAndUser;
import cz.fungisoft.coffeecompass.entity.StarsQualityDescription;
import cz.fungisoft.coffeecompass.exceptions.rest.ResourceNotFoundException;
import cz.fungisoft.coffeecompass.service.CoffeeSiteService;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import io.swagger.annotations.Api;

/**
 * REST Controller for handling addition/deletition/update of Comment and Stars for CoffeeSite.<br>
 * REST verze<br>
 * Obsluhuje operace souvisejici s vkladanim/mazanim/modifikaci hodnoceni a komentare ke CoffeeSitu.  
 * 
 * @author Michal Vaclavek
 *
 */
@Api // Swagger
@RestController 
@RequestMapping("/rest/secured/starsAndComments")
public class CSStarsCommentsControllerSecuredREST
{
    
    private static final Logger LOG = LoggerFactory.getLogger(CSStarsCommentsControllerSecuredREST.class);
    
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
     * @param id CoffeeSite id to which the StarsAndCommentModel belongs to
     * 
     * @return list of comments belonging to this coffeeSitie id
     */
    //@PostMapping(value="/saveStarsAndComment/{coffeeSiteId}", headers = {"content-type=application/json"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value="/saveStarsAndComment/{coffeeSiteId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CommentDTO>> saveCommentAndStarsForSite(@RequestBody @Valid StarsAndCommentModel starsAndComment,
                                                                       @PathVariable("coffeeSiteId") Long coffeeSiteId) {
        // Ulozit hodnoceni if not empty
        starsForCoffeeSiteService.saveStarsForCoffeeSiteAndLoggedInUser(coffeeSiteId, starsAndComment.getStars().getNumOfStars());
        
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
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
    
    
    /**
     * Zpracuje PUT pozadavek na update Commentu a Stars od daneho Usera pro dany Comment id.
     * <p>
     * Pro update jenom Stars lze take pouzit end pointy:<br>
     * /rest/secured/starsAndComments/updateStarsForCoffeeSiteAndUser/coffeeSite/{coffeeSiteId}/user/{userId}/stars/{numOfStars}
     * 
     *   JSON CommentDTO to update example:
     *   {
     *       "id": 59,
     *       "text": "sfsgsgsgsg",
     *       "created": "30.05. 2020 23:28",
     *       "coffeeSiteID": 204,
     *       "userName": "MichalV",
     *       "userId": 1,
     *       "canBeDeleted": false,
     *       "starsFromUser": 4
     *   }
     * 
     * @param comment Comment to be updated. Muze obsahovat prazdny text, pak je komentar smazan a odeslan status HttpStatus.NOT_FOUND
     * 
     * @return updatovany Comment nebo indikace chyby na vstupu (HttpStatus.BAD_REQUEST) nebo HttpStatus.NOT_FOUND,
     *         pokud byl Comment smazan nebo nebyl nalezen.
     */
    @PutMapping("/updateCommentAndStars")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommentDTO> updateCommentAndStarsForSite(@RequestBody CommentDTO comment) {
        
        // Ulozit updated Comment if not null
        if (comment != null) {
            // Ulozit updated Stars if not 0
            if (comment.getStarsFromUser() >= (StarsQualityDescription.StarsQualityEnum.ONE.ordinal() + 1)
                 && comment.getStarsFromUser() <= (StarsQualityDescription.StarsQualityEnum.FIVE.ordinal() + 1)
                 && comment.getUserId() > 0) {
                StarsForCoffeeSiteAndUser sfcsu = starsForCoffeeSiteService.updateStarsForCoffeeSiteAndUser(comment.getCoffeeSiteID(), comment.getUserId(), comment.getStarsFromUser());
                if (sfcsu != null) {
                    LOG.info("Stars updated for CoffeeSite id {}, from User id {}.", comment.getCoffeeSiteID(), comment.getUserId());
                } else {
                    LOG.error("Failed Stars update for CoffeeSite id {}, from User id {}", comment.getCoffeeSiteID(), comment.getUserId());
                }
            }
            
            Comment updatedComment = null;
            CommentDTO commentDTO = null;
            try {
                updatedComment = commentsService.updateComment(comment);
                
                if (updatedComment != null) {
                    commentDTO = commentsService.getByIdToTransfer(updatedComment.getId());
                    LOG.info("Comment updated for CoffeeSite id {}, from User id {}.", comment.getCoffeeSiteID(), comment.getUserId());
                } else {
                    LOG.error("Comment update failed for CoffeeSite id {}, from User id {}. Comment id {}", comment.getCoffeeSiteID(), comment.getUserId(), comment.getId());
                }
            } catch (Exception ex) {
                LOG.error("Error calling update Comment service.", ex);
                return new ResponseEntity<CommentDTO>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
            return (commentDTO != null) ? new ResponseEntity<CommentDTO>(commentDTO, HttpStatus.OK)
                                        : new ResponseEntity<CommentDTO>(HttpStatus.NOT_FOUND); // means not found or deleted
        }
        
        LOG.error("Error update Comment, input validation failed.");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    
    
    /**
     * Zpracuje PUT pozadavek na update hodnoceni Stars pro dane CoffeeSite id a daneho User id
     * 
     * @param numOfStars
     * @param coffeeSiteId
     * @param userId
     * 
     * @return 
     */
    @PutMapping("/updateStarsForCoffeeSiteAndUser/coffeeSite/{coffeeSiteId}/user/{userId}/stars/{numOfStars}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Integer> updateStarsForCoffeeSiteAndUser(@PathVariable(value="numOfStars", required=true) int numOfStars,
                                                                   @PathVariable(value="coffeeSiteId", required=true) Long coffeeSiteId,
                                                                   @PathVariable(value="userId", required=true) Long userId) {
        
        // Save updated Stars if not null
        if (numOfStars >= StarsQualityDescription.StarsQualityEnum.ONE.ordinal() + 1
               && numOfStars <= StarsQualityDescription.StarsQualityEnum.FIVE.ordinal() + 1) {
            StarsForCoffeeSiteAndUser starsForCoffeeSiteAndUser = starsForCoffeeSiteService.updateStarsForCoffeeSiteAndUser(coffeeSiteId, userId, numOfStars);
            
            return (starsForCoffeeSiteAndUser != null) ? new ResponseEntity<Integer>(starsForCoffeeSiteAndUser.getStars().getNumOfStars(), HttpStatus.OK)
                                                       : new ResponseEntity<Integer>(0, HttpStatus.BAD_REQUEST);
        }
        
        return new ResponseEntity<>(0, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Zpracuje DELETE pozadavek na smazani komentare k jednomu CoffeeSitu.<br>
     * Muze byt volano pouze ADMINEM nebo prihlasenym autorem hodnoceni s roli USER.<br>
     * Vrati aktualni pocet Comments pro coffee site, ke kteremu patril smazany komentar.<br>
     * 
     * @param id of the Comment to be deleted
     * @return number of Comments of the coffeeSite where the deleted comment belonged to
     */
    @DeleteMapping("/deleteComment/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Integer> deleteCommentAndStarsForSite(@PathVariable("commentId") Integer commentId) {
        
        Long siteId = null;
        siteId = commentsService.deleteCommentById(commentId);
        
        if (siteId == null) {
            throw new ResourceNotFoundException("Comment", "commentId", commentId);
        }
        Integer commentsNumber = commentsService.getNumberOfCommentsForSiteId(siteId);
        
        if (commentsNumber == null) {
            commentsNumber = 0;
        }
            
        return new ResponseEntity<>(commentsNumber, HttpStatus.OK);
    }

}
