package nl.wiegman.home.service;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.repository.KlimaatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class KlimaatServiceCached {

    @Autowired
    private KlimaatRepository klimaatRepository;

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getInPeriod(Date from, Date to) {
        return klimaatRepository.findByDatumtijdBetweenOrderByDatumtijd(from, to);
    }

}
