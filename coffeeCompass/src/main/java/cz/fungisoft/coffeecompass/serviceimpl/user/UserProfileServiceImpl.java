package cz.fungisoft.coffeecompass.serviceimpl.user;

import java.util.List;
import java.util.UUID;

import cz.fungisoft.coffeecompass.dto.UserProfileDTO;
import cz.fungisoft.coffeecompass.mappers.UserProfileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.exceptions.EntityNotFoundException;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.service.user.UserProfileService;

 
@Service("userProfileService")
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfRepository;

    private final UserProfileMapper userProfileMapper;
        
    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfRepository, UserProfileMapper userProfileMapper) {
        super();
        this.userProfRepository = userProfRepository;
        this.userProfileMapper = userProfileMapper;
    }

    @Override
//    @Cacheable(cacheNames = "userProfilesCache")

    public UserProfile findByExtId(String extId) {
        UserProfile userProfile = userProfRepository.findById(UUID.fromString(extId)).orElse(null);
        if (userProfile == null)
            throw new EntityNotFoundException("User profile id " + extId + " not found in DB.");
        return  userProfile;
    }
 
//    @Override
//    @Cacheable(cacheNames = "userProfilesCache")
//    public UserProfile findByType(String type) {
//        UserProfile userProfile = userProfRepository.searchByType(type);
//        if (userProfile == null)
//            throw new EntityNotFoundException("User profile " + type + " not found in DB.");
//        return userProfile;
//    }
 
    @Override
//    @Cacheable(cacheNames = "userProfilesCache")
    public List<UserProfileDTO> findAll() {
        return userProfRepository.findAll().stream().map(userProfileMapper::userProfiletoUserProfileDTO).toList();
    }
}