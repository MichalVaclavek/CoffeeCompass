package cz.fungisoft.coffeecompass.service.user;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.UserProfile;

public interface UserProfileService {

    UserProfile findById(Integer id);
    UserProfile findByType(String type);
    List<UserProfile> findAll();
}
