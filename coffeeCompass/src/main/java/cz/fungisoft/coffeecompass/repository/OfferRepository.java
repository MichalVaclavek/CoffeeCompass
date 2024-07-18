package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.OtherOffer;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;

public interface OfferRepository extends JpaRepository<OtherOffer, Integer> {

//    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select oo from OtherOffer oo where offer=?1")
    OtherOffer searchByName(String offer);
}
