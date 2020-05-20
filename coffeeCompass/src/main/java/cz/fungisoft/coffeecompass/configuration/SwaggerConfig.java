package cz.fungisoft.coffeecompass.configuration;

import io.swagger.annotations.Api;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger 2 to generate documentation for a Spring REST API
 * 
 * Osetri dotaz z http://localhost:8080/v2/api-docs
 * 
 *  http://localhost:8080/swagger-ui.html - zobrazuje graficky vysledek UI REST web sluzby
 *  dane RESTFull Web aplikace.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig
{
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                    .select()
                    .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                    .paths(PathSelectors.any())
                    .build().pathMapping("/")
                    .apiInfo(metaData());
    }
    
    private ApiInfo metaData() {

        Contact contact = new Contact("Michal Václavek", "https://coffeecompass.cz/about", "vaclavek.michal@gmail.com");

        return new ApiInfo(
                "Coffee compass",
                "Databáze lokalit Kávou s sebou",
                "1.0",
                "Terms of Service: nothing is guaranteed.",
                contact,
                "Apache License Version 2.0",
                "https://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList<>());
    }
}   
