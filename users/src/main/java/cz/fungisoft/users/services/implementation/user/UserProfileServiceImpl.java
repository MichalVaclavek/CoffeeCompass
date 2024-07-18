package cz.fungisoft.users.services.implementation.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service("userProfileService")
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfRepository;
        
    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfRepository) {
        super();
        this.userProfRepository = userProfRepository;
    }

    @Override
    public UserProfile findById(Integer id) {
        UserProfile userProfile = userProfRepository.findById(id).orElse(null);
        if (userProfile == null)
            throw new EntityNotFoundException("User profile id " + id + " not found in DB.");
        return  userProfile;
    }
 
    @Override
    public UserProfile findByType(String type) {
        UserProfile userProfile = userProfRepository.searchByType(type);
        if (userProfile == null)
            throw new EntityNotFoundException("User profile " + type + " not found in DB.");
        return userProfile;
    }
 
    @Override
    public List<UserProfile> findAll() {
        return userProfRepository.findAll();
    }
}