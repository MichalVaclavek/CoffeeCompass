package cz.fungisoft.coffeecompass.formatters;

import cz.fungisoft.coffeecompass.dto.UserProfileDTO;
import cz.fungisoft.coffeecompass.mappers.UserProfileMapper;
import cz.fungisoft.coffeecompass.service.user.UserProfileService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;

@Component
public class UserProfileDtoFormatter implements Formatter<UserProfileDTO> {

    private final UserProfileService userProfileService;

    private final UserProfileMapper userProfileMapper;

    @Autowired
    public UserProfileDtoFormatter(UserProfileService userProfileService, UserProfileMapper userProfileMapper) {
        super();
        this.userProfileService = userProfileService;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
    public String print(UserProfileDTO userProfile, Locale locale) {
        return userProfile.getExtId().toString();
    }

    @Override
    public UserProfileDTO parse(@NotNull String extId, Locale locale) throws ParseException {
        return userProfileMapper.userProfiletoUserProfileDTO(userProfileService.findByExtId(extId));
    }
}