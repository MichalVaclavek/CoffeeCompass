package cz.fungisoft.coffeecompass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Standardní vstupní bod Spring aplikace.
 * 
 * @author Michal Václavek
 */
@SpringBootApplication
@EnableFeignClients
public class CoffeeCompassApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoffeeCompassApplication.class, args);
	}		
}
