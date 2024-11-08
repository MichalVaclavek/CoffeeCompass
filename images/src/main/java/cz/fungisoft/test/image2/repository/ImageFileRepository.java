package cz.fungisoft.test.image2.repository;


import cz.fungisoft.test.image2.entity.ImageFileSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * JPA operations with ImageFiles entity.
 * 
 * @author Michal Vaclavek
 *
 */
public interface ImageFileRepository extends JpaRepository<ImageFileSet, Long> {

    Optional<ImageFileSet> findByExtId(String extId);

    List<ImageFileSet> findByImageObjectIdAndImageType(Long imageObjectId, String imageType);

//    @Query("DELETE from ImageFileSet imf WHERE extId=?1")
    void deleteByExtId(String extId);
}
