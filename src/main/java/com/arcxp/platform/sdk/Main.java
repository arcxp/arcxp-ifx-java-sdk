package com.arcxp.platform.sdk;

import com.arcxp.platform.sdk.utils.MapUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     * Shared Object Mapper for Serialization. Constructed as a bean so that @Autowired instances of
     * it use this instance.
     *
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        return MapUtils.createObjectMapper();
    }


}
