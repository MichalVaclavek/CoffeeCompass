package cz.fungisoft.coffeecompass.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cz.fungisoft.coffeecompass.entity.User;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

/**
 * JpaRepository<User, Long> - Long znamena ze primarni klic pro User je typu Long
 */
public interface UsersRepository extends JpaRepository<User, Long>, UsersRepositoryCustom {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select u from User u where userName= ?1")
    Optional<User> searchByUsername(String userName);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    @Query("select u from User u where email= ?1")
    Optional<User> searchByEmail(String email);
    
    @Query("delete from User u where userName= ?1") 
    void deleteByUserName(String userName);

    @Query("select count(id) from User u")
    Long countAllUsers();
    
    @Query("select count(id) from User u WHERE u.enabled=true")
    Long countAllEnabledUsers();
    
    @Query("select count(id) from User u WHERE date(u.createdOn) > (current_date - 7) AND u.enabled=true")
    Long getNumOfUsersRegisteredLast7Days();
}
