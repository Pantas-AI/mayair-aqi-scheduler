package com.pantasai.mayairaqi.scraper.feed;

import java.util.Map;

public record FeedData(String aqi, String idx, Map<String, Map<String, String>> iaqi, City city) {
}
