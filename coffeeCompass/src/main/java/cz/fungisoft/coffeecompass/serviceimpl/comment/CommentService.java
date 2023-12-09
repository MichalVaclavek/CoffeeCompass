/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl.comment;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cz.fungisoft.coffeecompass.exceptions.UserNotFoundException;
import cz.fungisoft.coffeecompass.mappers.CommentMapper;
import cz.fungisoft.coffeecompass.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CommentRepository;
import cz.fungisoft.coffeecompass.repository.CommentsPageableRepository;
import cz.fungisoft.coffeecompass.service.IStarsForCoffeeSiteAndUserService;
import cz.fungisoft.coffeecompass.service.comment.ICommentService;
import cz.fungisoft.coffeecompass.service.user.UserService;
import lombok.extern.log4j.Log4j2;


/**
 * Implementation of the ICommentService interface
 * 
 * @author Michal Václavek
 */
@Service("commentService")
@Transactional
@Log4j2
public class CommentService implements ICommentService {

    @Autowired
    private CoffeeSiteRepository coffeeSiteRepo;
    
    @Autowired
    private UserService userService;
    
    private final CommentRepository commentsRepo;
    
    @Autowired
    private CommentsPageableRepository commentsPageableRepo;
    
    private final CommentMapper commentMapper;

    /**
     * Used to add rating of the CoffeeSite from user to comments of the CoffeeSite from the same User
     */
    private IStarsForCoffeeSiteAndUserService starsForCoffeeSiteAndUserService;
		
	@Autowired
	public CommentService(CommentRepository commentsRepo,
                          CommentMapper commentMapper,
	                      IStarsForCoffeeSiteAndUserService starsForCoffeeSiteAndUserService) {
	    this.commentsRepo = commentsRepo;
        this.commentMapper = commentMapper;
	    this.starsForCoffeeSiteAndUserService = starsForCoffeeSiteAndUserService;
	}
	
	/**
	 * Can return null, if the User or CoffeeSite are not found based on it's id
	 */
	@Override
    public Comment saveTextAsComment(String commentText, Long userID, Long coffeeSiteID) {
	    Optional<User> user = userService.findById(userID);
        CoffeeSite cs = coffeeSiteRepo.findById(coffeeSiteID).orElse(null);             
        if (user.isPresent() && cs != null) {
            return saveTextAsComment(commentText, user.get(), cs);
        } else {
            return null;
        }
    }

    @Override
    public Comment saveTextAsComment(String commentText, User user, CoffeeSite coffeeSite) {
        if ( (commentText == null) || commentText.isEmpty()) {
            throw new IllegalArgumentException("Empty comment text is not allowed!");
        }
        
        Comment comment = new Comment();
        comment.setCreated(LocalDateTime.now());
        comment.setText(commentText);
        comment.setUser(user);
        comment.setCoffeeSite(coffeeSite);
       
        commentsRepo.save(comment);
        
        log.info("Comment saved. User name: {}, Coffee site name: {}", user.getUserName(), coffeeSite.getSiteName());
        
        return comment;
     }
    
    @Override
    public Comment saveTextAsComment(String commentText, CoffeeSite coffeeSite) {
        Optional<User> loggedInUser = userService.getCurrentLoggedInUser();
        return loggedInUser.map(user -> saveTextAsComment(commentText, user, coffeeSite))
                           .orElseThrow(() -> new UserNotFoundException("Comment not saved, user not logged in."));
    }
    
    /**
     * Updates Comment object.
     * <p>
     * Can return null if the text of updated comment is empty
     * or the updatedComment* and respective Comment in DB to be updated (based on id) does not match,
     * i.e. User id* or CoffeeSite id of the updatedComments is not the same as the respective Comment in DB.
     */
    @Override
    public Comment updateComment(CommentDTO updatedComment) {
        
        if (updatedComment != null && updatedComment.getCoffeeSiteID() > 0 && updatedComment.getUserId() > 0) {
            
            Optional<Comment> comment = commentsRepo.findById(updatedComment.getId());
            
            if (comment.isPresent()) {
                if (updatedComment.getText().isEmpty()) { // empty text of Comment means delete Comment
                    commentsRepo.deleteById(updatedComment.getId());
                    log.info("Comment update. Comment id {} deleted, because of empty comment text. User id {}, CoffeeSite id {}", updatedComment.getId(),  updatedComment.getUserId(), updatedComment.getCoffeeSiteID());
                    return null;
                }
                
                if (comment.get().getUser().getId() == updatedComment.getUserId()
                        && comment.get().getCoffeeSite().getId() == updatedComment.getCoffeeSiteID()) {
                    comment.get().setText(updatedComment.getText());
                    comment.get().setCreated(LocalDateTime.now());
                    log.info("Comment id {} updated from User id {} for CoffeeSite id {}", updatedComment.getId(),  updatedComment.getUserId(), updatedComment.getCoffeeSiteID());
                    return commentsRepo.save(comment.get());
                }
            }
        } // if any input data are wrong, log it
        if (updatedComment != null) {
            log.warn("Comment id {} update failed. User id {} for CoffeeSite id {}", updatedComment.getId(),  updatedComment.getUserId(), updatedComment.getCoffeeSiteID());
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see cz.zutrasoft.base.services.CommentService#getAllCommentsFromUser(int)
	 */
	@Override
	public List<CommentDTO> getAllCommentsFromUser(Long userID) {
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
    public Page<CommentDTO> findAllCommentsForSitePaginated(CoffeeSite coffeeSite, Pageable pageable) {
	    Page<Comment> commentsPage = commentsPageableRepo.findByCoffeeSite(coffeeSite, pageable);
        return commentsPage.map(this::modifyToTransfer);
    }
	
	@Override
    public Integer getNumberOfCommentsForSiteId(Long siteId) {
        return commentsRepo.getNumberOfCommentsForSite(siteId);
    }

	@Override
	public List<CommentDTO> getAllComments() {
	    return modifyToTransfer(commentsRepo.findAll());
	}
	
	 
	@Override
    public Page<CommentDTO> findAllCommentsPaginated(Pageable pageable) {
	    Page<Comment> commentsPage = commentsPageableRepo.findAll(pageable);
        return commentsPage.map(this::modifyToTransfer);
    }
	    

	
	/**
     * Adds attributes to CoffeeSite to identify what operations can be done with Comment in UI
     * 
     * @param comments
     * @return list of CommentDTO to be sent to client
     */
    private List<CommentDTO> modifyToTransfer(List<Comment> comments) {
        
        return comments.stream().map(this::modifyToTransfer).collect(Collectors.toList());
    }
    
    /**
     * Adds attributes to CoffeeSite to identify what operations can be done with Comment in UI.
     * Also adds Stars from user for this CoffeeSite.
     * 
     * @param comment
     * @return list of CommentDTO to be sent to client
     */
    private CommentDTO modifyToTransfer(Comment comment) {
        CommentDTO commentToTransfer = commentMapper.commentToCommentDTO(comment);
        
        commentToTransfer.setCanBeDeleted(isDeletable(commentToTransfer));
        commentToTransfer.setStarsFromUser(starsForCoffeeSiteAndUserService.getStarsForCoffeeSiteAndUser(commentToTransfer.getCoffeeSiteID(), commentToTransfer.getUserId()));
        
        return commentToTransfer;
    }
    
    private boolean isDeletable(CommentDTO comment) {
          return comment != null &&  userService.getCurrentLoggedInUser().isPresent()
                  &&  (userService.isADMINloggedIn() || comment.getUserName().equals(userService.getCurrentLoggedInUser().get().getUserName()));
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
        if (comment == null) {
            throw new EntityNotFoundException("Comment with id " + id + " not found.");
        }
        return comment;
    }
    
    @Override
    public CommentDTO getByIdToTransfer(Integer id) {
        return modifyToTransfer(getById(id));
    }

    @Override
    public void deleteAllCommentsFromUser(Long userID) {
        log.info("All comments from user id {} deleted.", userID);
        commentsRepo.deleteAllFromUser(userID);
    }

    @Override
    public void deleteAllCommentsForSite(Long coffeeSiteID) {
        log.info("All comments of the Coffee site id {} deleted.", coffeeSiteID);
        commentsRepo.deleteAllForSite(coffeeSiteID);
    }

}
