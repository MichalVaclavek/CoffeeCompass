package cz.fungisoft.users.services.user;

import cz.fungisoft.users.entity.UserProfile;

import java.util.List;

public interface UserProfileService {

    UserProfile findById(Integer id);
    UserProfile findByType(String type);
    List<UserProfile> findAll();
}
