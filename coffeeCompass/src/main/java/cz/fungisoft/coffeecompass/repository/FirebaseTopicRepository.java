package cz.fungisoft.coffeecompass.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.FirebaseTopic;

/**
 * Interface pro ukladani/cteni objektu typu FirebaseTopic do DB.
 * 
 * @author Michal Vaclavek
 */
public interface FirebaseTopicRepository extends JpaRepository<FirebaseTopic, Integer> {
    
    /**
     * Gets all Topics
     * 
     * @return
     */
    @Query("select ft from FirebaseTopic ft")
    public List<FirebaseTopic> getAllFirebaseTopics();
    
    /**
     * Gets given Topic and subTopic
     * 
     * @return
     */
    @Query("select ft from FirebaseTopic ft WHERE mainTopic=?1 AND subTopic=?2")
    public Optional<FirebaseTopic> getOneTopicSubtopic(String topic, String subTopic);
    
    
    /**
     * Delete Topic and sub Topic
     */
    @Modifying // required by Hibernate, otherwise there is an exception ' ... Illegal state ...'
    @Query("delete FROM FirebaseTopic ft where mainTopic=?1 AND subTopic=?2")
    public void deleteTopic(String topic, String subTopic);
    
}
