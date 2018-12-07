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
    public List<Comment> getAllCommentsForSite(Integer coffeeSiteID);
    
    @Query("select cl from Comment cl where user.id=?1")
    public List<Comment> getAllCommentsFromUser(Integer userID);
    
    /*
    @Query("delete FROM Comment cl where user.id=?1")
    public void deleteAllFromUser(Integer userID);

    @Query("delete FROM Comment cl where coffeeSite.id=?1")
    public void deleteAllForSite(Integer siteID);
 */
}
