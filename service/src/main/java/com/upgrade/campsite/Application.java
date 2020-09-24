package com.upgrade.campsite;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;


@OpenAPIDefinition(
        info = @Info(
                title = "Campsite",
                version = "1.0",
                description = "campsite reservation tech challenge ",
                contact = @Contact(url = "https://github.com/linthar/campsite_tech_challenge", name = "Fernando Garcia")
        )
)
public class Application {
    public static void main(String[] args) {
        Micronaut.run(args);
    }
}
