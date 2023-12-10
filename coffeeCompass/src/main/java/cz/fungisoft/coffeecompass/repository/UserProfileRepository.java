package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Query("SELECT up FROM UserProfile up WHERE type=?1")
    UserProfile searchByType(String type);
}
