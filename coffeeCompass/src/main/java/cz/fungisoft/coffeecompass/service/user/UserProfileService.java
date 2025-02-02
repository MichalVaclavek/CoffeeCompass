package cz.fungisoft.coffeecompass.service.user;

import java.util.List;

import cz.fungisoft.coffeecompass.dto.UserProfileDTO;
import cz.fungisoft.coffeecompass.entity.UserProfile;

public interface UserProfileService {

    UserProfile findByExtId(String extId);
    List<UserProfileDTO> findAll();
}
