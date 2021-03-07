package cz.fungisoft.coffeecompass.service.comment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Comment getById(Integer id);
    public CommentDTO getByIdToTransfer(Integer id);
    
    /**
	 * Saves the text of comment into DB.
	 * 
	 * @param commentText text of {@link Comment} object to be saved.
	 * @param userID userID of the logged-in user
	 * @param coffeeSiteID id of the {@link CoffeeSite} the comment text belongs to
	 * 
	 * @return instance of the saved {@link Comment} object.
	 */
	public Comment saveTextAsComment(String commentText, Long userID, Long coffeeSiteID);
	public Comment saveTextAsComment(String commentText, User user, CoffeeSite coffeeSite);
	public Comment saveTextAsComment(String commentText, CoffeeSite coffeeSite);
	
	/**
	 * Updates Comment (in DB) sent from client as CommentDTO
	 * 
	 * @param updatedComment CommentDTO to be updated in DB.
	 * @return Comment if it was updated successfuly, null otherwise
	 */
	public Comment updateComment(CommentDTO updatedComment);
    
	/**
	 * Gets list of all saved Comments for given CoffeeSite id.
	 * 
	 * @param siteId id of the CoffeeSite for which the comments are required.
	 * @return all Comments for given CoffeeSite id
	 */
	public List<CommentDTO> getAllCommentsForSiteId(Long siteId);
	
	public Page<CommentDTO> findAllCommentsForSitePaginated(CoffeeSite coffeeSite, Pageable pageable);
	
	
	/**
     * Gets number of all saved Comments for given CoffeeSite id.
     * 
     * @param siteId id of the CoffeeSite for which the comments are required.
     * @return all Comments for given CoffeeSite id
     */
    public Integer getNumberOfCommentsForSiteId(Long siteId);   
	
	/**
	 * Gets lis of all saved Comments from given {@code User}
	 * 
	 * @param userID id of the User the comments are required from.
	 * @return all Comments of given userID i.e. User with this id. 
	 */
	public List<CommentDTO> getAllCommentsFromUser(Long userID);
	public List<CommentDTO> getAllCommentsFromUser(User user);
	
	/**
	 * Gets list all saved Comments.
	 * 
	 * @return list all saved Comments
	 */
	public List<CommentDTO> getAllComments();
	
	public Page<CommentDTO> findAllCommentsPaginated(Pageable pageable);
	
	/**
	 * Deletes persistent {@link Comment} object from DB	
	 * @param comment {@link Comment} object to be deleted from repository/DB.
	 * @retutn id of the CoffeeSite the deleted Comment belongs to.
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public Long deleteComment(Comment comment);
	
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public Long deleteCommentById(Integer commentId);
	
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public void deleteAllCommentsFromUser(Long userID);
	
	@PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_DBA')")
	public void deleteAllCommentsForSite(Long coffeeSiteID);
}
