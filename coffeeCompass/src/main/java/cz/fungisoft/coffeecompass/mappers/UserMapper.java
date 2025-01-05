package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.dto.UserProfileDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(uses = {UserProfileMapper.class})
public interface UserMapper {

    @Mapping(target = "id", source="extId")
    @Mapping(target = "userProfiles", expression = "java(mapUserProfileDTOsToUserProfiles(userDTO.getUserProfiles()))")
    User userDTOtoUser(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "extId", source="id")
    @Mapping(target = "userProfiles", expression = "java(mapUserProfilesToUserProfileDTOs(user.getUserProfiles()))")
    UserDTO usertoUserDTO(User user);

    Set<UserProfileDTO> mapUserProfilesToUserProfileDTOs(Set<UserProfile> userProfiles);

    Set<UserProfile> mapUserProfileDTOsToUserProfiles(Set<UserProfileDTO> userProfileDTOs);
}
