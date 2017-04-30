package nl.wiegman.home.klimaat;

import nl.wiegman.home.klimaat.Klimaat;
import nl.wiegman.home.klimaat.KlimaatRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class KlimaatServiceCached {

    private final KlimaatRepos klimaatRepository;

    @Autowired
    public KlimaatServiceCached(KlimaatRepos klimaatRepository) {
        this.klimaatRepository = klimaatRepository;
    }

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getInPeriod(String klimaatSensorCode, Date from, Date to) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode, from, to);
    }
}
