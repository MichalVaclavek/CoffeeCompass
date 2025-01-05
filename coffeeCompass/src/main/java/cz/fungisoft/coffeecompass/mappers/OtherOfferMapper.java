package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.OtherOfferDTO;
import cz.fungisoft.coffeecompass.entity.OtherOffer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OtherOfferMapper {

    @Mapping(target = "id", source="extId")
    @Mapping(source = "otherOffer", target = "offer")
//    @Mapping(target = "longId", ignore = true)
    OtherOffer otherOfferDtoToOtherOffer(OtherOfferDTO otherOfferDTO);

    @Mapping(source = "offer", target = "otherOffer")
    @Mapping(target = "extId", source="id")
    OtherOfferDTO otherOfferToOtherOfferDto(OtherOffer otherOffer);

}


