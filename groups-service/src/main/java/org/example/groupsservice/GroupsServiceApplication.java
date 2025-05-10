package org.example.groupsservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@OpenAPIDefinition(
        info = @Info(
                title = "Group API",
                version = "1.0",
                description = "Documentation Group API v1.0"
        )
)
public class GroupsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupsServiceApplication.class, args);
    }

    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
