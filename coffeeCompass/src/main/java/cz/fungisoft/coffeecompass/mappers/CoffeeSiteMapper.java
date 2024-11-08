package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.*;
import cz.fungisoft.coffeecompass.entity.*;
import cz.fungisoft.coffeecompass.serviceimpl.PriceRangeServiceImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
//import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;

@Mapper(imports = {UUID.class},
        uses = {CoffeeSiteTypeMapper.class, CoffeeSiteStatusMapper.class, CoffeeSiteRecordStatusMapper.class,
                CoffeeSortMapper.class, CompanyMapper.class, CupTypeMapper.class, NextToMachineTypeMapper.class,
                OtherOfferMapper.class, PriceRangeMapper.class, SiteLocationTypeMapper.class, UserMapper.class,
         })
public interface CoffeeSiteMapper {

    @Mapping(source = "originalUser.userName", target = "originalUserName")
    @Mapping(source = "lastEditUser.userName", target = "lastEditUserName")
    @Mapping(target = "cupTypes", expression = "java(mapCupTypesToCupTypeDTOs(coffeeSite.getCupTypes()))")
    @Mapping(target = "otherOffers", expression = "java(mapOtherOffersToOtherOfferDTOs(coffeeSite.getOtherOffers()))")
    @Mapping(target = "nextToMachineTypes", expression = "java(mapNextToMachineTypesToNextToMachineTypeDTOs(coffeeSite.getNextToMachineTypes()))")
    @Mapping(target = "coffeeSorts", expression = "java(mapCoffeeSortsToCoffeeSortDTOs(coffeeSite.getCoffeeSorts()))")
    @Mapping(target = "extId", source="id")
    CoffeeSiteDTO coffeeSiteToCoffeeSiteDTO(CoffeeSite coffeeSite);

    @Mapping(target = "cupTypes", expression = "java(mapCupTypeDTOsToCupTypes(coffeeSiteDTO.getCupTypes()))")
    @Mapping(target = "otherOffers", expression = "java(mapOtherOfferDTOsToOtherOffers(coffeeSiteDTO.getOtherOffers()))")
    @Mapping(target = "nextToMachineTypes", expression = "java(mapNextToMachineTypeDTOsToNextToMachineTypes(coffeeSiteDTO.getNextToMachineTypes()))")
    @Mapping(target = "coffeeSorts", expression = "java(mapCoffeeSortDTOsToCoffeeSorts(coffeeSiteDTO.getCoffeeSorts()))")
    @Mapping(target = "id", source="extId")
    CoffeeSite coffeeSiteDtoToCoffeeSite(CoffeeSiteDTO coffeeSiteDTO);

    Set<CupType> mapCupTypeDTOsToCupTypes(Set<CupTypeDTO> cupTypeDTOs);

    Set<CupTypeDTO> mapCupTypesToCupTypeDTOs(Set<CupType> cupTypes);


    Set<OtherOffer> mapOtherOfferDTOsToOtherOffers(Set<OtherOfferDTO> otherOfferDTOs);

    Set<OtherOfferDTO> mapOtherOffersToOtherOfferDTOs(Set<OtherOffer> otherOffers);

    Set<NextToMachineType> mapNextToMachineTypeDTOsToNextToMachineTypes(Set<NextToMachineTypeDTO> nextToMachineTypeDTOs);

    Set<NextToMachineTypeDTO> mapNextToMachineTypesToNextToMachineTypeDTOs(Set<NextToMachineType> nextToMachineTypes);

    Set<CoffeeSort> mapCoffeeSortDTOsToCoffeeSorts(Set<CoffeeSortDTO> coffeeSortDTOs);

    Set<CoffeeSortDTO> mapCoffeeSortsToCoffeeSortDTOs(Set<CoffeeSort> coffeeSorts);
}
