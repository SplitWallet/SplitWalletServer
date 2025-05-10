package org.example.expensesservice;

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
                title = "Expense API",
                version = "1.0",
                description = "Documentation Expense API v1.0"
        )
)
public class ExpensesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpensesServiceApplication.class, args);
    }

    @Bean
    ModelMapper modelMapper(){
        return new ModelMapper();
    }

}
