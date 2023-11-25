package cz.fungisoft.coffeecompass.mappers;

import cz.fungisoft.coffeecompass.dto.ContactMeMessageDTO;
import cz.fungisoft.coffeecompass.entity.ContactMeMessage;
import org.mapstruct.Mapper;

@Mapper
public interface ContactMeMessageMapper {

    ContactMeMessage contactMeMessageDtoToContactMeMessage(ContactMeMessageDTO contactMeMessageDTO);
}
