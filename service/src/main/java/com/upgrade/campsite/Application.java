package com.upgrade.campsite;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

import java.util.TimeZone;


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
        // MySQL dates are stored using UTC tz
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Micronaut.run(args);
    }
}
