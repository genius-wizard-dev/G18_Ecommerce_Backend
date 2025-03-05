package com.g18.ecommerce.ProfileService.configurations;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfiguration {
    @Bean
    Gson gson() {
        return new Gson();
    }
}
