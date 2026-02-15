package com.example.contentgenerator.config;

import com.example.contentgenerator.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF for APIs
            .csrf(csrf -> csrf.disable())

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                        "/", 
                        "/login**",
                        "/oauth2/**",
                        "/error",
                        "/actuator/**",
                        "/webjars/**",
                        "/api/marketing/**",   // your AI API
                        "/api/public/**"
                ).permitAll()

                // Protect only user info
                .requestMatchers("/api/user").authenticated()

                // Everything else optional (change later)
                .anyRequest().permitAll()
            )

            // OAuth2 Login Config
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> 
                    userInfo.userService(customOAuth2UserService)
                )
                .defaultSuccessUrl("/api/user", true)
            );

        return http.build();
    }
}
