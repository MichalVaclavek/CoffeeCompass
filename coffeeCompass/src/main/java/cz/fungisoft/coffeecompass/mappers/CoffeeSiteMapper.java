package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Configuration;

@Mapper(uses = {DateMapper.class})
public interface CoffeeSiteMapper {

    @Mapping(source = "originalUser.userName", target = "originalUserName")
    @Mapping(source = "lastEditUser.userName", target = "lastEditUserName")
    CoffeeSiteDTO coffeeSiteToCoffeeSiteDTO(CoffeeSite coffeeSite);

    CoffeeSite coffeeSiteDtoToCoffeeSite(CoffeeSiteDTO coffeeSiteDTO);
}
