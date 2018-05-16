package nl.homeserver.klimaat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource("/integrationtests.properties")
public class KlimaatRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private KlimaatRepos klimaatRepository;

    @Test
    public void whenDeleteByKlimaatSensorThenDeleted() {
        String klimaatSensorCode = "GARDEN";
        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode(klimaatSensorCode);

        entityManager.persist(klimaatSensor);

        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(15)).withKlimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(10)).withKlimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(5)).withKlimaatSensor(klimaatSensor).build());

        assertThat(klimaatRepository.findAll()).hasSize(3);

        klimaatRepository.deleteByKlimaatSensorCode(klimaatSensorCode);

        assertThat(klimaatRepository.findAll()).isEmpty();
    }
}