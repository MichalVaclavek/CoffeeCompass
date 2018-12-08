/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.repository.CoffeeSiteRepository;
import cz.fungisoft.coffeecompass.repository.CommentRepository;
import cz.fungisoft.coffeecompass.repository.UsersRepository;
import cz.fungisoft.coffeecompass.service.ICommentService;
import cz.fungisoft.coffeecompass.service.UserService;


/**
 * Singleton implementation of the ICommentService interface
 * 
 * @author Michal Václavek
 */
@Service("commentService")
@Transactional
public class CommentService implements ICommentService
{
    @Autowired
    UsersRepository userRepo;
    
    @Autowired
    CoffeeSiteRepository coffeeSiteRepo;
    
    @Autowired
    private UserService userService;
   
    private CommentRepository commentsRepo;
		
	@Autowired
	public CommentService(CommentRepository commentsRepo) {
	    this.commentsRepo = commentsRepo;
	}
	
	@Override
    public Comment saveTextAsComment(String commentText, Integer userID, Integer coffeeSiteID) {
        User user = userRepo.findById(userID).orElse(null);
              
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
	public List<Comment> getAllCommentsFromUser(Integer userID) {
		return commentsRepo.getAllCommentsFromUser(userID);
	}

	@Override
	public List<Comment> getAllCommentsFromUser(User user) {
	    return commentsRepo.getAllCommentsFromUser(user.getId());
	}

	@Override
	public List<Comment> getAllComments() {
		return commentsRepo.findAll();
	}

	@Override
	public Integer deleteComment(Comment comment) {
	    return deleteCommentById(comment.getId());
	}
	
	@Override
    public Integer deleteCommentById(Integer commentId) {
	    Integer siteId = commentsRepo.getSiteIdForComment(commentId);
	    commentsRepo.deleteById(commentId);
	    return siteId;
    }

    @Override
    public Comment getById(Integer id) {
        return commentsRepo.findById(id).orElse(null);
    }

    @Override
    public List<Comment> getAllCommentsForSiteId(Integer coffeeSiteID) {
        return commentsRepo.getAllCommentsForSite(coffeeSiteID);
    }

    @Override
    public void deleteAllCommentsFromUser(Integer userID) {
        commentsRepo.deleteAllFromUser(userID);
        
    }

    @Override
    public void deleteAllCommentsForSite(Integer coffeeSiteID) {
        commentsRepo.deleteAllForSite(coffeeSiteID);
    }

}
