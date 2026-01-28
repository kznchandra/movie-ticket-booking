package com.pbs.bookingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbs.bookingservice.common.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public UserDetails userPrincipal() {
        return new UserPrincipal();
    }
}
