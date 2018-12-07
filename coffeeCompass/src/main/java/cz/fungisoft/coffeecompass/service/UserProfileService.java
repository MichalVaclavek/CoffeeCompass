package cz.fungisoft.coffeecompass.service;

import java.util.List;

import cz.fungisoft.coffeecompass.entity.UserProfile;

 
public interface UserProfileService
{
    public UserProfile findById(Integer id);
    public UserProfile findByType(String type);
    public List<UserProfile> findAll();     
}
