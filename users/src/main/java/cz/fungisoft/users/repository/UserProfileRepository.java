package cz.fungisoft.users.repository;


public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    @Query("SELECT up FROM UserProfile up WHERE type=?1")
    UserProfile searchByType(String type);
}
