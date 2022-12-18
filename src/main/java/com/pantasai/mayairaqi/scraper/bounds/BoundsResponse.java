package com.pantasai.mayairaqi.scraper.bounds;

import java.util.List;

public record BoundsResponse(List<BoundsData> data, String dt, String status) {
}
