package nl.wiegman.home.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import nl.wiegman.home.model.KlimaatSensor;
import nl.wiegman.home.repository.KlimaatSensorRepository;

@Component
public class Bootstrap implements CommandLineRunner {

    private static final String WOONKAMER_CODE = "WOONKAMER";
    private static final String WOONKAMER_OMSCHRIJVING = "Woonkamer";

    @Autowired
    KlimaatSensorRepository klimaatSensorRepository;

    @Override
    public void run(String... args) throws Exception {
        KlimaatSensor woonkamerSensor = klimaatSensorRepository.findFirstByCodeIgnoreCase(WOONKAMER_CODE);
        if (woonkamerSensor == null) {
            woonkamerSensor = new KlimaatSensor();
            woonkamerSensor.setOmschrijving(WOONKAMER_OMSCHRIJVING);
            woonkamerSensor.setCode(WOONKAMER_CODE);
            klimaatSensorRepository.save(woonkamerSensor);
        }
    }
}
