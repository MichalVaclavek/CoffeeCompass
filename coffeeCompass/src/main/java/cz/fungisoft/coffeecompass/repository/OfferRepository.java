package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.OtherOffer;

public interface OfferRepository extends JpaRepository<OtherOffer, Integer> {

    @Query("select oo from OtherOffer oo where offer=?1")
    OtherOffer searchByName(String offer);
}
