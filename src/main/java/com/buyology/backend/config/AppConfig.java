package com.buyology.backend.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableMethodSecurity
public class AppConfig {

    @Bean // basically telling the spring here is the object I made for you and you manage it.
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    public WebClient.Builder webClientBuilder(){ return WebClient.builder();}
}
