package com.buyology.backend.config;

// We are writting this class because allthe api's are protected by the jwt so you cant test the api's without configure!

import com.buyology.backend.security.services.SecurityService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP) //which tells this is a hhtp based schema
                                .scheme("bearer") // tells swagger to use the bearer auth schema
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token"))); // deeply nested, harder to read
    }
}

//new SecurityRequirement().addList("bearerAuth") this tells this security schema is required for the accessing the api.
// with the @Tag annotation we can group the endpoints