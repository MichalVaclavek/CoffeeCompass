package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "id", source="extId")
    User userDTOtoUser(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "extId", source="id")
    UserDTO usertoUserDTO(User user);
}
