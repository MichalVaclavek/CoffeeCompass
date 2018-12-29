/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exception.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CommentRepository;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.UserService;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFacade;


/**
 * Singleton implementation of the ICommentService interface
 * 
 * @author Michal Václavek
 */
@Service("commentService")
@Transactional
@Log4j2
public class CommentService implements ICommentService
{
//    private static final Logger logger = LogManager.getLogger(CommentService.class);
    
    @Autowired
    CoffeeSiteRepository coffeeSiteRepo;
    
    @Autowired
    private UserService userService;
   
    private CommentRepository commentsRepo;
    private MapperFacade mapperFacade;
		
	@Autowired
	public CommentService(CommentRepository commentsRepo, MapperFacade mapperFacade) {
	    this.commentsRepo = commentsRepo;
	    this.mapperFacade = mapperFacade;
	}
	
	@Override
    public Comment saveTextAsComment(String commentText, Integer userID, Long coffeeSiteID) {
	    User user = userService.findById(userID);
        CoffeeSite cs = coffeeSiteRepo.findById(coffeeSiteID).orElse(null);             
       
        return saveTextAsComment(commentText, user, cs);
    }

    @Override
    public Comment saveTextAsComment(String commentText, User user, CoffeeSite coffeeSite) {
        if ( (commentText == null) || commentText.isEmpty())
            throw new IllegalArgumentException("Empty comment text is not allowed!");
        
        Comment comment = new Comment();
        comment.setCreated(new Timestamp(new Date().getTime()));
        comment.setText(commentText);
        comment.setUser(user);
        comment.setCoffeeSite(coffeeSite);
       
        commentsRepo.save(comment);
        
        log.info("Comment saved. User name: {}, Coffee site name: {}", user.getUserName(), coffeeSite.getSiteName());
        
        return comment;
     }
    
    @Override
    public Comment saveTextAsComment(String commentText, CoffeeSite coffeeSite) {
        User logedInUser = userService.getCurrentLoggedInUser();
        return saveTextAsComment(commentText, logedInUser, coffeeSite);
    }

	/* (non-Javadoc)
	 * @see cz.zutrasoft.base.services.CommentService#getAllCommentsFromUser(int)
	 */
	@Override
	public List<CommentDTO> getAllCommentsFromUser(Integer userID) {
	    return modifyToTransfer(commentsRepo.getAllCommentsFromUser(userID));
	}

	@Override
	public List<CommentDTO> getAllCommentsFromUser(User user) {
	    return modifyToTransfer(commentsRepo.getAllCommentsFromUser(user.getId()));
	}
	
	@Override
    public List<CommentDTO> getAllCommentsForSiteId(Long coffeeSiteID) {
	    return modifyToTransfer(commentsRepo.getAllCommentsForSite(coffeeSiteID));
    }

	@Override
	public List<CommentDTO> getAllComments() {
	    return modifyToTransfer(commentsRepo.findAll());
	}
	
	private CommentDTO convertCommentToDto(Comment comment) {
	    CommentDTO retValComment = mapperFacade.map(comment, CommentDTO.class);
	    retValComment.setCanBeDeleted(isDeletable(retValComment));
	    return retValComment;
	}
	
	/**
     * Adds attributes to CoffeeSite to identify what operations can be done with CoffeeSite in UI
     * 
     * @param sites
     * @return
     */
    private List<CommentDTO> modifyToTransfer(List<Comment> comments) {
        List<CommentDTO> commentsTransfer = mapperFacade.mapAsList(comments, CommentDTO.class);
        
        for (CommentDTO comment : commentsTransfer) {
            comment.setCanBeDeleted(isDeletable(comment));
        }
        
        return commentsTransfer;
    }
    
    private boolean isDeletable(CommentDTO comment) {
        return (comment != null && userService.getCurrentLoggedInUser() != null) ? 
                  userService.isADMINloggedIn() || comment.getUserName().equals(userService.getCurrentLoggedInUser().getUserName()) : 
                     false;
    }

	@Override
	public Long deleteComment(Comment comment) {
	    return deleteCommentById(comment.getId());
	}
	
	/**
	 * {@inheritDoc}
	 * Returns CoffeeSite the deleted Comment belonged to.
	 */
	@Override
    public Long deleteCommentById(Integer commentId) {
	    Long siteId = commentsRepo.getSiteIdForComment(commentId);
	    commentsRepo.deleteById(commentId);
	    log.info("Comment deleted. Id {}", commentId);
	    return siteId;
    }

    @Override
    public Comment getById(Integer id) {
        Comment comment = commentsRepo.findById(id).orElse(null);
        if (comment == null)
            throw new EntityNotFoundException("Comment with id " + id + " not found.");
        return comment;
    }

    @Override
    public void deleteAllCommentsFromUser(Integer userID) {
        log.info("All comments from user id {} deleted.", userID);
        commentsRepo.deleteAllFromUser(userID);
        
    }

    @Override
    public void deleteAllCommentsForSite(Long coffeeSiteID) {
        log.info("All comments of the Coffee site id {} deleted.", coffeeSiteID);
        commentsRepo.deleteAllForSite(coffeeSiteID);
    }

}
