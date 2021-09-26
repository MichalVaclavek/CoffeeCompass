package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Query("select up from UserProfile up where type=?1")
    UserProfile searchByType(String type);
}
