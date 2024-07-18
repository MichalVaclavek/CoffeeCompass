package cz.fungisoft.test.image2.repository;


import cz.fungisoft.test.image2.entity.ImageObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * JPA operations with ImageObject entity.
 * 
 * @author Michal Vaclavek
 *
 */
public interface ImageObjectRepository extends JpaRepository<ImageObject, Long> {

//    @Query("{ 'externalObjectId': ?1}")
//    @Query("SELECT ImageObject imo FROM ImageObject WHERE externalObjectId=?1")
    Optional<ImageObject> findByExternalObjectId(String extObjectId);

    @Modifying
    @Query("delete FROM ImageObject io where externalObjectId=?1")
    void deleteByExternalObjectId(String externalObjectId);
}
