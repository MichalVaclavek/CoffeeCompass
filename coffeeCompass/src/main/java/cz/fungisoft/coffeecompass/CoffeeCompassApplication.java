package cz.fungisoft.coffeecompass;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import cz.fungisoft.coffeecompass.dto.CoffeeSiteDto;
import cz.fungisoft.coffeecompass.dto.UserDataDto;
import cz.fungisoft.coffeecompass.entity.CoffeeSite;
import cz.fungisoft.coffeecompass.entity.User;

/**
 * Standardní vstupní bod aplikace.
 * 
 * @author Michal Václavek
 */
@EnableAutoConfiguration
@SpringBootApplication
public class CoffeeCompassApplication
{
    /**
	 * Interface a jeho implementace pro object, ktery se pouziva pro "mapovani" mezi zakladnimi Entity objekty
	 * a prislusnymi DTO objekty, ktere se posilaji z Repository do Serice a View/Controller vrstvy.
	 * 
	 * @return
	 */
    @Bean
	public MapperFacade mapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        
        // Uprava pro mapovano z User na UserDataDTO - pro prenaseni na clienta neni potreba prenaset heslo a confirm hesla
        mapperFactory.classMap(User.class, UserDataDto.class).exclude("password")
                                                             .exclude("confirmPassword")
                                                             .byDefault()
                                                             .register();
        
        mapperFactory.classMap(UserDataDto.class, User.class).byDefault().register();

        // Only userName is needed for CoffeeSiteDto object
        mapperFactory.classMap(CoffeeSite.class, CoffeeSiteDto.class)
                                        .field("originalUser.userName", "originalUserName")
                                        .field("lastEditUser.userName", "lastEditUserName")
                                        .byDefault()
                                        .register();
     
        return mapperFactory.getMapperFacade();
	}	   
	
	public static void main(String[] args) {
		SpringApplication.run(CoffeeCompassApplication.class, args);
	}		

}
