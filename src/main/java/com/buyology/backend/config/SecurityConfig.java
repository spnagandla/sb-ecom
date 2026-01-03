package com.buyology.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception{

        return httpSecurity
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                // Require authentication for EVERY request
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                // Enable JWT validation (Authorization: Bearer <token>), verifies with Supabase keys
                .oauth2ResourceServer(oauth -> oauth.jwt(withDefaults()))

                .build();
    }
}
