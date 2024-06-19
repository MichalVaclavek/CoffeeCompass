/**
 * 
 */
package cz.fungisoft.coffeecompass.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.ContactMeMessage;

/**
 * @author Michal Vaclavek
 */
public interface ContactMeMessageRepository extends JpaRepository<ContactMeMessage, Integer> {

    @Query("select cmm from ContactMeMessage cmm where authorName=?1")
    List<ContactMeMessage> getAllFromAuthor(String authorName);
    
    /**
     * Gets all ContactMeMessage created from Date to now.
     * 
     * @param dateFrom
     * @return
     */
    @Query("select cmm from ContactMeMessage cmm where createdTime > ?1")
    List<ContactMeMessage> getAllFromDate(LocalDateTime dateFrom);
}
