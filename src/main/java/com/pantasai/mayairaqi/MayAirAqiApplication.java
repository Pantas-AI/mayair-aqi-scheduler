package com.pantasai.mayairaqi;

import com.pantasai.mayairaqi.scraper.AqiScraper;
import com.pantasai.mayairaqi.scraper.Station;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class MayAirAqiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MayAirAqiApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(AqiScraper aqiScraper) {
        return (args) -> {
//            aqiScraper.scrapeAllStation();
        };
    }

}
