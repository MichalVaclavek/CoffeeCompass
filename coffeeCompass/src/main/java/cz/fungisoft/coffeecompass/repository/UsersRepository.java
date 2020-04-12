package cz.fungisoft.coffeecompass.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.User;

/**
 * JpaRepository<User, Integer> - Integer znamena ze primarni klic pro User je typu Integer
 */
public interface UsersRepository extends JpaRepository<User, Long>, UsersRepositoryCustom
{
    @Query("select u from User u where userName= ?1")
    public Optional<User> searchByUsername(String userName);
    
    @Query("select u from User u where email= ?1")
    public Optional<User> searchByEmail(String email);
    
    @Query("delete from User u where userName= ?1") 
    public void deleteByUserName(String userName);

    @Query("select count(id) from User u")
    public Long countAllUsers();
    
    @Query("select count(id) from User u WHERE u.enabled=true")
    public Long countAllEnabledUsers();
    
    @Query("select count(id) from User u WHERE date(u.createdOn) > (current_date - 7) AND u.enabled=true")
    public Long getNumOfUsersRegisteredLast7Days();

}
