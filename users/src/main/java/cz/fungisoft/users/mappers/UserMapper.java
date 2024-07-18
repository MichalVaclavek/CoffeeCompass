package cz.fungisoft.users.mappers;

@Mapper
public interface UserMapper {

    User userDTOtoUser(UserDTO userDTO);

    @Mapping(target = "password", ignore = true)
    UserDTO usertoUserDTO(User user);
}
