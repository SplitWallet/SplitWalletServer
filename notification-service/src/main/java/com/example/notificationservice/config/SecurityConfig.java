package com.example.notificationservice.config;

import org.springframework.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import com.example.notificationservice.security.KeycloakJwtTokenConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtTokenConverter keycloakJwtTokenConverter;

    @Autowired
    public SecurityConfig(TokenConverterProperties properties) {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter
                = new JwtGrantedAuthoritiesConverter();
        this.keycloakJwtTokenConverter
                = new KeycloakJwtTokenConverter(
                jwtGrantedAuthoritiesConverter,
                properties);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/*/notifications", "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/*/tokens").permitAll()
                        .requestMatchers("/**").authenticated())
                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtCustomizer -> jwtCustomizer.jwtAuthenticationConverter(keycloakJwtTokenConverter)))
                .build();
    }

    /**
     * Конфигурация cors.
     * Позволяет ограничить запросы из других доменов.
     * В данном случае разрешены все типы запросов от любых доменов.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}