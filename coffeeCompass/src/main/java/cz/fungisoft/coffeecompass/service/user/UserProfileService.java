package cz.fungisoft.coffeecompass.service.user;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.UserProfileDTO;
import cz.fungisoft.coffeecompass.entity.UserProfile;

public interface UserProfileService {

    UserProfile findByExtId(String extId);
    UserProfile findByType(String type);
    List<UserProfileDTO> findAll();
}
