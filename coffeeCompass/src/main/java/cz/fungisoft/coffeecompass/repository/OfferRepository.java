package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.OtherOffer;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<OtherOffer, UUID> {

    @Query("select oo from OtherOffer oo where offer=?1")
    OtherOffer searchByName(String offer);

//    @Query("select oo from OtherOffer oo where id=?1")
//    Optional<OtherOffer> findByExtId(UUID extId);

    @Query("select oo from OtherOffer oo where longId=?1")
    Optional<OtherOffer> findByLongId(Integer id);
}
