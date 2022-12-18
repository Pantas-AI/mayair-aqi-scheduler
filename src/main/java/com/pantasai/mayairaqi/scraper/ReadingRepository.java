package com.pantasai.mayairaqi.scraper;

import com.pantasai.mayairaqi.Readings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingRepository extends MongoRepository<Readings, String> {
    List<Readings> findByStationOrderByTimestampAsc(String station);
}
