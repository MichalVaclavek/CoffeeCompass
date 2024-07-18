package cz.fungisoft.coffeecompass.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.Comment;

/**
 * Interface pro ukladani/cteni objektu typu Comment do DB.
 * 
 * @author Michal Vaclavek
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select cl from Comment cl where coffeeSite.id=?1 order by cl.created desc")
    List<Comment> getAllCommentsForSite(Long coffeeSiteID);
    
    @Query("select count(*) from Comment cl where coffeeSite.id=?1")
    Integer getNumberOfCommentsForSite(Long coffeeSiteID);
    
    @Query("select cl from Comment cl where user.id=?1 order by cl.created desc")
    List<Comment> getAllCommentsFromUser(Long userID);

    /**
     * Number of all Comments, used to estimate the size of Comments when dowanloading for Offline mode.
     * @return number of all Comments in DB
     */
    @Query("select count(*) from Comment")
    Long getNumberOfAllComments();

    
    /**
     * Gets CoffeeSite id this Comment belongs to
     * @param commentId
     */
    @Query("select coffeeSite.id FROM Comment cl where id=?1")
    Long getSiteIdForComment(Long commentId);
    
    @Modifying // required by Hibernate, otherwise there is an exception ' ... Illegal state ...'
    @Query("delete FROM Comment cl where user.id=?1")
    void deleteAllFromUser(Long userID);

    @Query("delete FROM Comment cl where coffeeSite.id=?1")
    void deleteAllForSite(Long siteID);
}
