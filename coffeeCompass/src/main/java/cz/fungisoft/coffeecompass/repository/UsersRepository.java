package cz.fungisoft.coffeecompass.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.CrossOrigin;

import cz.fungisoft.coffeecompass.entity.User;

/**
 * JpaRepository<CoffeeSite, Integer> - Integer znamena ze primarni klic pro CoffeeSite je typu Integer
 */
//@CrossOrigin
public interface UsersRepository extends JpaRepository<User, Integer>
{
    @Query("select u from User u where userName= ?1")
    public User searchByUsername(String userName);
    
    @Query("select u from User u where email= ?1")
    public User searchByEmail(String email);
    
    @Query("delete from User u where userName= ?1") 
    public void deleteByUserName(String userName);

    @Query(nativeQuery = true, value = "select count(*) from User")
    public long countItems();
}
