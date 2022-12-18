package com.pantasai.mayairaqi.scraper;

import com.pantasai.mayairaqi.Readings;
import com.pantasai.mayairaqi.scraper.bounds.BoundsRequest;
import com.pantasai.mayairaqi.scraper.bounds.BoundsResponse;
import com.pantasai.mayairaqi.scraper.feed.FeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AqiScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AqiScraper.class);
    private static final BoundsRequest BOUNDS_REQUEST = new BoundsRequest("-180,-180,180,180", "", "placeholders", "webgl", 8);

    private final RestTemplate restTemplate;
    private final ReadingRepository readingRepository;
    private final WAqiConfig config;

    public AqiScraper(ReadingRepository readingRepository, WAqiConfig config) {
        this.readingRepository = readingRepository;
        this.restTemplate = new RestTemplate();
        this.config = config;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void scrapeAllStation() {
        getStationList().forEach(station -> {
            String url = String.format("https://api.waqi.info/feed/@%s/?token=%s", station.idx(), config.getToken());
            try {
                System.out.println("test");
                FeedResponse response = restTemplate.getForObject(url, FeedResponse.class);
                Readings readings = toReading(station, response);
                readingRepository.save(readings);
            } catch (Exception e) {
                LOGGER.error(station.toString(), e);
            }
        });
    }

    private Readings toReading(Station station, FeedResponse response) {
        Map<String, String> values = new HashMap<>();
        values.put("aqi", response.data().aqi());
        for (Map.Entry<String, Map<String, String>> data : response.data().iaqi().entrySet()) {
            values.put(data.getKey(), data.getValue().get("v"));
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return new Readings(station.idx(), now, values);
    }

    public List<Station> getStationList() {
        BoundsResponse boundsResponse = restTemplate.postForObject("https://api.waqi.info/mapq2/bounds", BOUNDS_REQUEST, BoundsResponse.class);
        Stream<Station> stationList = boundsResponse.data().stream()
                .map(boundsData -> new Station(boundsData.idx(), boundsData.name()));
        return stationList.collect(Collectors.toList());
    }



}
