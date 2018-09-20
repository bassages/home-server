package nl.homeserver.klimaat;

import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import nl.homeserver.RepositoryIntegrationTest;

public class KlimaatRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private KlimaatRepos klimaatRepository;

    @Test
    public void whenDeleteByKlimaatSensorThenDeleted() {
        final String klimaatSensorCode = "GARDEN";
        final KlimaatSensor klimaatSensor = KlimaatSensorBuilder.aKlimaatSensor().withCode(klimaatSensorCode).build();

        entityManager.persist(klimaatSensor);

        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(15)).withKlimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(10)).withKlimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(5)).withKlimaatSensor(klimaatSensor).build());

        assertThat(klimaatRepository.findAll()).hasSize(3);

        klimaatRepository.deleteByKlimaatSensorCode(klimaatSensorCode);

        assertThat(klimaatRepository.findAll()).isEmpty();
    }
}