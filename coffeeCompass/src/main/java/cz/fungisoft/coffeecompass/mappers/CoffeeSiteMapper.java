package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDTO;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Mapper(imports = UUID.class)
public interface CoffeeSiteMapper {

    @Mapping(source = "originalUser.userName", target = "originalUserName")
    @Mapping(source = "lastEditUser.userName", target = "lastEditUserName")
    CoffeeSiteDTO coffeeSiteToCoffeeSiteDTO(CoffeeSite coffeeSite);

    @Mapping(target = "externalId", expression = "java(!coffeeSiteDTO.getExternalId().isEmpty() ? UUID.fromString(coffeeSiteDTO.getExternalId()) : null)")
    CoffeeSite coffeeSiteDtoToCoffeeSite(CoffeeSiteDTO coffeeSiteDTO);
}
