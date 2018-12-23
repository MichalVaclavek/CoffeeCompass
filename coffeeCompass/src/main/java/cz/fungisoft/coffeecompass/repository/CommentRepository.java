package cz.fungisoft.coffeecompass.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Comment;

/**
 * Interface pro ukladani objektu typu Comment do DB.
 * 
 * @author Michal Vaclavek
 */
public interface CommentRepository extends JpaRepository<Comment, Integer>
{
    @Query("select cl from Comment cl where coffeeSite.id=?1")
    public List<Comment> getAllCommentsForSite(Long coffeeSiteID);
    
    @Query("select cl from Comment cl where user.id=?1")
    public List<Comment> getAllCommentsFromUser(Integer userID);
    
    /**
     * Gets CoffeeSite id this Comment belongs to
     * @param commentID
     */
    @Query("select coffeeSite.id FROM Comment cl where id=?1")
    public Long getSiteIdForComment(Integer commentId);
    
    @Query("delete FROM Comment cl where user.id=?1")
    public void deleteAllFromUser(Integer userID);

    @Query("delete FROM Comment cl where coffeeSite.id=?1")
    public void deleteAllForSite(Long siteID);
 
}
