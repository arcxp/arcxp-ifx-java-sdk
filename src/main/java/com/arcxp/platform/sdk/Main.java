package com.arcxp.platform.sdk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


/**
 * Spring Boot main entry pont class.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.arcxp.platform.sdk", "${basePackage}"})
public class Main {
    public static void main(String[] args) {
        String envName = System.getenv("ENV") != null ? System.getenv("ENV") : "local";
        SpringApplication springApp = new SpringApplication(Main.class);
        springApp.setAdditionalProfiles(envName);
        springApp.run(args);
    }
    
    /**
     * Shared Object Mapper for Serialization.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .build();
        return objectMapper;
    }


}
