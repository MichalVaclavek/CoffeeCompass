package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CommentDTO;
import cz.fungisoft.coffeecompass.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class})
public interface CommentMapper {

    @Mapping(source ="coffeeSite.id", target = "coffeeSiteId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "extId", source="id")
    @Mapping(target = "userName", source="user.userName")
    CommentDTO commentToCommentDTO(Comment comment);
}
