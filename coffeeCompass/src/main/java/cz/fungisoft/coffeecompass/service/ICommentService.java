package cz.fungisoft.coffeecompass.service;

import java.util.List;

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
	public Comment saveTextAsComment(String commentText, int userID, int coffeeSiteID);
	public Comment saveTextAsComment(String commentText, User user, CoffeeSite coffeeSite);
	public Comment saveTextAsComment(String commentText, CoffeeSite coffeeSite);
    
	public Comment getById(int id);
	
	/**
	 * Gets list of all saved Comments for given Article id.
	 * 
	 * @param artId id of the Article for which the comments are required.
	 * @return all Comments for given Article id
	 */
	public List<Comment> getAllCommentsForSiteId(int artId);	
	
	/**
	 * Gets lis of all saved Comments from given {@code User}
	 * 
	 * @param userID id of the User the comments are required from.
	 * @return all Comments of given userID i.e. User with this id. 
	 */
	public List<Comment> getAllCommentsFromUser(int userID);
	public List<Comment> getAllCommentsFromUser(User user);
	
	/**
	 * Gets list all saved Comments.
	 * 
	 * @return list all saved Comments
	 */
	public List<Comment> getAllComments();
	
	/**
	 * Deletes persistent {@link Comment} object from DB	
	 * @param comment {@link Comment} object to be deleted from repository/DB.
	 */
	public void deleteComment(Comment comment);
	
	/*
	public void deleteAllCommentsFromUser(Integer userID);
	public void deleteAllCommentsForSite(Integer coffeeSiteID);
    */
}
