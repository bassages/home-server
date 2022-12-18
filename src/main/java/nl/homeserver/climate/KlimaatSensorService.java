package nl.homeserver.climate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KlimaatSensorService {

    private final KlimaatSensorRepository klimaatSensorRepository;

    KlimaatSensor getOrCreateIfNonExists(final String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode)
                                      .orElseGet(() -> createKlimaatSensor(klimaatSensorCode));
    }

    private KlimaatSensor createKlimaatSensor(final String klimaatSensorCode) {
        final KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(klimaatSensorCode);
        klimaatSensor.setOmschrijving(null);
        return klimaatSensorRepository.save(klimaatSensor);
    }

    Optional<KlimaatSensor> getByCode(final String klimaatSensorCode) {
        return klimaatSensorRepository.findFirstByCode(klimaatSensorCode);
    }

    List<KlimaatSensor> getAll() {
        return klimaatSensorRepository.findAll();
    }

    KlimaatSensor save(final KlimaatSensor klimaatSensor) {
        return klimaatSensorRepository.save(klimaatSensor);
    }

    public void delete(final KlimaatSensor klimaatSensor) {
        this.klimaatSensorRepository.delete(klimaatSensor);
    }
}
