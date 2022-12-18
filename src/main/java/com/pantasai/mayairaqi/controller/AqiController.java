package com.pantasai.mayairaqi.controller;

import com.pantasai.mayairaqi.Readings;
import com.pantasai.mayairaqi.scraper.ReadingRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AqiController {

    private final ReadingRepository repository;

    public AqiController(ReadingRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/station/{id}/pastData")
    public PastDataResponse getStationPastData(@PathVariable String id) throws InvalidStationException {
        List<Readings> readings = repository.findByStationOrderByTimestampAsc(id);

        if (readings.isEmpty())
            throw new InvalidStationException("Invalid station");

        String station = readings.get(0).station();

        Map<String, PastDataList> map = new HashMap<>();
        for (Readings reading : readings) {
            Map<String, String> values = reading.values();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                if (!map.containsKey(key)) {
                    map.put(key, new PastDataList(key, new ArrayList<>()));
                }

                map.get(key).values().add(new TimestampGraph(reading.timestamp(), entry.getValue()));
            }
        }

        return new PastDataResponse(station, new HashSet<>(map.values()));
    }

    @ExceptionHandler(value = {
            InvalidStationException.class
    })
    public void handleException(Exception e, HttpServletResponse response) throws IOException {
        response.setStatus(403);
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        response.getWriter().write(e.getMessage());
    }
}
