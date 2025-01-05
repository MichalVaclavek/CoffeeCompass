package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.UserProfileDTO;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserProfileMapper {

    @Mapping(target = "id", source="extId")
    UserProfile userProfileDTOtoUserProfile(UserProfileDTO userDTO);

    @Mapping(target = "extId", source="id")
    UserProfileDTO userProfiletoUserProfileDTO(UserProfile userProfile);
}
