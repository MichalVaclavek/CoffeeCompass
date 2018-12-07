package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.List;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.fungisoft.coffeecompass.entity.UserProfile;
import cz.fungisoft.coffeecompass.repository.UserProfileRepository;
import cz.fungisoft.coffeecompass.service.UserProfileService;

 
@Service("userProfileService")
@Transactional
public class UserProfileServiceImpl implements UserProfileService
{
    private UserProfileRepository userProfRepository;
        
    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfRepository) {
        super();
        this.userProfRepository = userProfRepository;
    }

    @Override
    public UserProfile findById(Integer id) {
        return userProfRepository.findById(id).orElse(null);
    }
 
    @Override
    public UserProfile findByType(String type) {
        return userProfRepository.searchByType(type);
    }
 
    @Override
    public List<UserProfile> findAll() {
        return userProfRepository.findAll();
    }
}