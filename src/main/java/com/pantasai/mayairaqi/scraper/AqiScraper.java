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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class AqiScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AqiScraper.class);
    private static final BoundsRequest BOUNDS_REQUEST = new BoundsRequest("-180,-90,180,90", "", "placeholders", "webgl", 8);

    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final ReadingRepository readingRepository;
    private final WAqiConfig config;

    public AqiScraper(ReadingRepository readingRepository, WAqiConfig config) {
        this.webClient = WebClient.create();
        this.readingRepository = readingRepository;
        this.restTemplate = new RestTemplate();
        this.config = config;
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void scrapeAllStation() {
        List<Readings> readings = getStationFlux()
                .flatMap(station -> {
                    String url = String.format("https://api.waqi.info/feed/@%s/?token=%s", station.idx(), config.getToken());
                    return webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(FeedResponse.class)
                            .onErrorComplete();
                })
                .map(this::toReading)
                .collectList()
                .onErrorResume(e -> Mono.just(new ArrayList<>()))
                .block();

        readingRepository.saveAll(readings);
    }

    private Readings toReading(FeedResponse response) {
        Map<String, String> values = new HashMap<>();
        values.put("aqi", response.data().aqi());
        for (Map.Entry<String, Map<String, String>> data : response.data().iaqi().entrySet()) {
            values.put(data.getKey(), data.getValue().get("v"));
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        return new Readings(response.data().idx(), now, values);
    }

    public Set<Station> getStationList() {
        Flux<Station> stationFlux = getStationFlux();

        return stationFlux
                .collect(Collectors.toSet())
                .block();
    }

    private Flux<Station> getStationFlux() {
        Set<int[]> boundSet = IntStream.range(-180, 180)
                .mapToObj(startLat ->
                        IntStream.range(-90, 90).mapToObj(
                                startLong -> new int[]{startLat, startLong, startLat + 1, startLong + 1}
                        ).collect(Collectors.toSet())
                ).flatMap(data -> data.stream())
                .collect(Collectors.toSet());

        return Flux.fromIterable(boundSet)
                .flatMap(bound -> getStationSetFluxByBound(bound[0], bound[1], bound[2], bound[3]))
                .flatMapIterable(stations -> stations);
    }

    private Mono<Set<Station>> getStationSetFluxByBound(int startLat, int startLong, int endLat, int endLong) {
        String latLong = String.format("%s,%s,%s,%s", startLat, startLong, endLat, endLong);
        BoundsRequest request = new BoundsRequest(latLong, "", "placeholders", "webgl", 8);

        return webClient.post()
                .uri("https://api.waqi.info/mapq2/bounds")
                .body(Mono.just(request), BoundsRequest.class)
                .retrieve()
                .bodyToMono(BoundsResponse.class)
                .map(this::responseToStationSet);
    }

    private Set<Station> getStationListByBound(int startLat, int startLong, int endLat, int endLong) {
        String latLong = String.format("%s,%s,%s,%s", startLat, startLong, endLat, endLong);
        BoundsRequest request = new BoundsRequest(latLong, "", "placeholders", "webgl", 8);
        BoundsResponse boundsResponse = restTemplate.postForObject("https://api.waqi.info/mapq2/bounds", request, BoundsResponse.class);

        return responseToStationSet(boundsResponse);
    }

    private Set<Station> responseToStationSet(BoundsResponse response) {
        return response.data().stream()
                .map(boundsData -> new Station(boundsData.idx(), boundsData.name()))
                .collect(Collectors.toSet());
    }



}
