package com.pantasai.mayairaqi.scraper;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "waqi")
@Data
public class WAqiConfig {
    private String token;
}
