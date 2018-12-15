package cz.fungisoft.coffeecompass.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.Comment;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Basic service methods to work with Comments (of CoffeeSites)
 * 
 * @author Michal Václavek
 */
public interface ICommentService
{
	/**
	 * Saves the text of comment into DB.
	 * 
	 * @param commentText text of {@link Comment} object to be saved.
	 * @param userID userID of the logged-in user
	 * @param coffeeSiteID id of the {@link CoffeeSite} the comment text belongs to
	 * 
	 * @return instance of the saved {@link Comment} object.
	 */
	public Comment saveTextAsComment(String commentText, Integer userID, Integer coffeeSiteID);
	public Comment saveTextAsComment(String commentText, User user, CoffeeSite coffeeSite);
	public Comment saveTextAsComment(String commentText, CoffeeSite coffeeSite);
    
	public Comment getById(Integer id);
	
	/**
	 * Gets list of all saved Comments for given Article id.
	 * 
	 * @param siteId id of the CoffeeSite for which the comments are required.
	 * @return all Comments for given CoffeeSite id
	 */
	public List<CommentDTO> getAllCommentsForSiteId(Integer siteId);	
	
	/**
	 * Gets lis of all saved Comments from given {@code User}
	 * 
	 * @param userID id of the User the comments are required from.
	 * @return all Comments of given userID i.e. User with this id. 
	 */
	public List<CommentDTO> getAllCommentsFromUser(Integer userID);
	public List<CommentDTO> getAllCommentsFromUser(User user);
	
	/**
	 * Gets list all saved Comments.
	 * 
	 * @return list all saved Comments
	 */
	public List<CommentDTO> getAllComments();
	
	/**
	 * Deletes persistent {@link Comment} object from DB	
	 * @param comment {@link Comment} object to be deleted from repository/DB.
	 * @retutn id of the CoffeeSite the deleted Comment belongs to.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public Integer deleteComment(Comment comment);
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public Integer deleteCommentById(Integer commentId);
	
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public void deleteAllCommentsFromUser(Integer userID);
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public void deleteAllCommentsForSite(Integer coffeeSiteID);
    
}
