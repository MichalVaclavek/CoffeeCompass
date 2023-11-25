package cz.fungisoft.coffeecompass.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;


@Configuration
public class Swagger3Config {

    @Value("${cz.fungisoft.openapi.dev-url}")
    private String devUrl;

    @Value("${cz.fungisoft.openapi.prod-url}")
    private String prodUrl;

    @Bean
    public OpenAPI coffeeCompassOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Server URL in Development environment");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("Server URL in Production environment");

        Contact contact = new Contact();
        contact.setEmail("vaclavek.michal@gmail.com");
        contact.setName("Michal Václavek");
        contact.setUrl("https://www.coffeecompass.cz");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Coffee compass")
                .version("1.0")
                .contact(contact)
                .description("Databáze lokalit Kávy s sebou").termsOfService("https://coffeecompass.cz/about")
                .license(mitLicense);

        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
    }
}