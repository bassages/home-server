package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.repository.KlimaatRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class KlimaatServiceCached {

    @Autowired
    KlimaatRepos klimaatRepository;

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getInPeriod(String klimaatSensorCode, Date from, Date to) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode, from, to);
    }
}
