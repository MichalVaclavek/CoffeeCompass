package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    User userDTOtoUser(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    UserDTO usertoUserDTO(User user);
}
