package cz.fungisoft.coffeecompass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Standardní vstupní bod Spring aplikace.
 * 
 * @author Michal Václavek
 */
@SpringBootApplication
public class CoffeeCompassApplication
{
	public static void main(String[] args) {
		SpringApplication.run(CoffeeCompassApplication.class, args);
	}		
}
