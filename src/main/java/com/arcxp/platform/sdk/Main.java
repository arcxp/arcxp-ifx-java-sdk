package com.arcxp.platform.sdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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


}
