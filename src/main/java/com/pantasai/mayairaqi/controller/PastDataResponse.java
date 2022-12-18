package com.pantasai.mayairaqi.controller;

import java.util.Set;

public record PastDataResponse(String station, Set<PastDataList> data) {
}
