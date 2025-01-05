package cz.fungisoft.coffeecompass.service.comment;

import java.util.List;
import java.util.UUID;

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
public interface ICommentService {

	Comment getByExtId(String id);
	Comment getByExtId(UUID id);

	CommentDTO getByExtIdToTransfer(String id);
	CommentDTO getByExtIdToTransfer(UUID id);
    
    /**
	 * Saves the text of comment into DB.
	 * 
	 * @param commentText text of {@link Comment} object to be saved.
	 * @param userExtId userID of the logged-in user
	 * @param coffeeSiteExtId id of the {@link CoffeeSite} the comment text belongs to
	 * 
	 * @return instance of the saved {@link Comment} object.
	 */
	Comment saveTextAsComment(String commentText, String userExtId, String coffeeSiteExtId);
	Comment saveTextAsComment(String commentText, User user, CoffeeSite coffeeSite);
	Comment saveTextAsComment(String commentText, CoffeeSite coffeeSite);
	
	/**
	 * Updates Comment (in DB) sent from client as CommentDTO
	 * 
	 * @param updatedComment CommentDTO to be updated in DB.
	 * @return Comment if it was updated successfuly, null otherwise
	 */
	Comment updateComment(CommentDTO updatedComment);
    
	/**
	 * Gets list of all saved Comments for given CoffeeSite id.
	 * 
	 * @param coffeeSiteId id of the CoffeeSite for which the comments are required.
	 * @return all Comments for given CoffeeSite id
	 */

	List<CommentDTO> getAllCommentsForSiteId(UUID coffeeSiteId);

	List<CommentDTO> getAllCommentsForSiteId(String siteExtId);
	
	Page<CommentDTO> findAllCommentsForSitePaginated(CoffeeSite coffeeSite, Pageable pageable);
	
	
	/**
     * Gets number of all saved Comments for given CoffeeSite id.
     * 
     * @param siteExtId id of the CoffeeSite for which the comments are required.
     * @return all Comments for given CoffeeSite id
     */
    Integer getNumberOfCommentsForSiteId(UUID siteExtId);

	Integer getNumberOfCommentsForSiteId(String siteExtId);

	/**
	 * Gets lis of all saved Comments from given {@code User}
	 * 
	 * @param userExtID id of the User the comments are required from.
	 * @return all Comments of given userID i.e. User with this id.
	 */
	List<CommentDTO> getAllCommentsFromUser(String userExtID);
	List<CommentDTO> getAllCommentsFromUser(UUID userExtID);
	List<CommentDTO> getAllCommentsFromUser(User user);
	
	/**
	 * Gets list all saved Comments.
	 * 
	 * @return list all saved Comments
	 */
	List<CommentDTO> getAllComments();
	
	Page<CommentDTO> findAllCommentsPaginated(Pageable pageable);
	
	/**
	 * Deletes persistent {@link Comment} object from DB	
	 * @param comment {@link Comment} object to be deleted from repository/DB.
	 * @retutn id of the CoffeeSite the deleted Comment belongs to.
	 */
	@PreAuthorize("hasRole('ADMIN') OR hasRole('DBA')")
	UUID deleteComment(Comment comment);
	
	@PreAuthorize("hasRole('ADMIN') OR hasRole('DBA')")
	UUID deleteCommentByExtId(UUID commentId);

	@PreAuthorize("hasRole('ADMIN') OR hasRole('DBA')")
	UUID deleteCommentByExtId(String commentId);
	
	@PreAuthorize("hasRole('ADMIN') OR hasRole('DBA')")
	void deleteAllCommentsFromUser(UUID userExtID);
}
