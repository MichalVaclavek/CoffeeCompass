package cz.fungisoft.coffeecompass.service;

import java.util.Date;
import java.util.List;

import cz.fungisoft.coffeecompass.dto.ContactMeMessageDto;
import cz.fungisoft.coffeecompass.entity.ContactMeMessage;

public interface IContactMeMessageService
{
    public ContactMeMessage saveContactMeMessage(String message, String authorName, String email);
    public ContactMeMessage saveContactMeMessage(ContactMeMessageDto message);
    public ContactMeMessage saveContactMeMessage(ContactMeMessage message);
    
    public List<ContactMeMessage> getAllFromAuthor(String authorName);
    
    /**
     * Gets all ContactMeMessage created after fromDate
     * @param fromDate
     * @return
     */
    public List<ContactMeMessage> getAllFromDate(Date fromDate);
    
    public List<ContactMeMessage> getAll();
}
