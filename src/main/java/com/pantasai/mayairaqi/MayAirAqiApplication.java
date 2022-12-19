package com.pantasai.mayairaqi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MayAirAqiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MayAirAqiApplication.class, args);
    }

}
