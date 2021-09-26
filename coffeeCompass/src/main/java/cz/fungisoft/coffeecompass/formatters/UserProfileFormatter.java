package cz.fungisoft.coffeecompass.formatters;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.service.user.UserProfileService;

@Component
public class UserProfileFormatter implements Formatter<UserProfile> {

    private final UserProfileService userProfileService;
    
    @Autowired
    public UserProfileFormatter(UserProfileService userProfileService) {
        super();
        this.userProfileService = userProfileService;
    }

    @Override
    public String print(UserProfile userProfile, Locale locale) {
        return (userProfile != null ? Integer.toString(userProfile.getId()) : "");
    }

    @Override
    public UserProfile parse(String text, Locale locale) throws ParseException {
        return userProfileService.findByType(text);
    }
}