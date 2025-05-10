package org.example.splitwalletserver;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SplitWalletServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitWalletServerApplication.class, args);
    }

    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
