package cz.fungisoft.coffeecompass.service;

import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass.dto.ContactMeMessageDTO;
import cz.fungisoft.coffeecompass.entity.ContactMeMessage;

public interface IContactMeMessageService {

    ContactMeMessage saveContactMeMessage(String message, String authorName, String email);
    ContactMeMessage saveContactMeMessage(ContactMeMessageDTO message);
    ContactMeMessage saveContactMeMessage(ContactMeMessage message);
    
    List<ContactMeMessage> getAllFromAuthor(String authorName);
    
    /**
     * Gets all ContactMeMessage created after fromDate
     * @param fromDate
     * @return
     */
    List<ContactMeMessage> getAllFromDate(Date fromDate);
    
    List<ContactMeMessage> getAll();
}
