package org.example.splitwalletserver.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI api(){
        return new OpenAPI()
                .servers(
                        List.of(
                                new Server().url("http://localhost:6868")
                        )
                )
                .info(
                        new Info().title("SplitWalletServer")
                                .description("No.")
                ).components(new Components()
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите Bearer токен в формате 'Bearer {token}'"))
        )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}