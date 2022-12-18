package com.pantasai.mayairaqi;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Document(collection = "readings")
public record Readings(String station, LocalDateTime timestamp, Map<String, String> values) {
}
