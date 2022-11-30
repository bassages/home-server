package nl.homeserver.klimaat;

import static nl.homeserver.klimaat.KlimaatBuilder.aKlimaat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import nl.homeserver.RepositoryIntegrationTest;

class KlimaatRepositoryIntegrationTest extends RepositoryIntegrationTest {

    static final String SENSOR_CODE_GARDEN = "GARDEN";

    @Autowired
    KlimaatRepos klimaatRepository;

    @Test
    void whenDeleteByKlimaatSensorThenDeleted() {
        final KlimaatSensor klimaatSensor = persistSensor(SENSOR_CODE_GARDEN);

        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(15)).withKlimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(10)).withKlimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().withDatumtijd(LocalDateTime.now().minusMinutes(5)).withKlimaatSensor(klimaatSensor).build());

        assertThat(klimaatRepository.findAll()).hasSize(3);

        klimaatRepository.deleteByKlimaatSensorCode(SENSOR_CODE_GARDEN);

        assertThat(klimaatRepository.findAll()).isEmpty();
    }

    @Test
    void whenGetEarliestLowestTemperatureOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor(SENSOR_CODE_GARDEN);

        LocalDate day = LocalDate.of(2022, 1, 1);

        // Not lowest temperature
        Klimaat klimaat1 = aKlimaat()
                .withDatumtijd(day.atTime(10, 0))
                .withTemperatuur(new BigDecimal("20.01"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat1);

        // Lowest temperature on day and earliest
        Klimaat klimaat2 = aKlimaat()
                .withDatumtijd(day.atTime(10, 1))
                .withTemperatuur(new BigDecimal("20.00"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat2);

        // Lowest temperature on day, but not earliest
        Klimaat klimaat3 = aKlimaat()
                .withDatumtijd(day.atTime(10, 2))
                .withTemperatuur(new BigDecimal("20.00"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat3);

        Klimaat result = klimaatRepository.earliestLowestTemperatureOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    @Test
    void whenGetEarliestHighestTemperatureOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor(SENSOR_CODE_GARDEN);

        LocalDate day = LocalDate.of(2022, 1, 1);

        // Not highest temperature
        Klimaat klimaat1 = aKlimaat()
                .withDatumtijd(day.atTime(10, 0))
                .withTemperatuur(new BigDecimal("19.99"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat1);

        // Highest temperature on day and earliest
        Klimaat klimaat2 = aKlimaat()
                .withDatumtijd(day.atTime(10, 1))
                .withTemperatuur(new BigDecimal("20.00"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat2);

        // Highest temperature on day, but not earliest
        Klimaat klimaat3 = aKlimaat()
                .withDatumtijd(day.atTime(10, 2))
                .withTemperatuur(new BigDecimal("20.00"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat3);

        Klimaat result = klimaatRepository.earliestHighestTemperatureOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    @Test
    void whenGetEarliestLowestHumidityOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor(SENSOR_CODE_GARDEN);

        LocalDate day = LocalDate.of(2022, 1, 1);

        // Not lowest humidity
        Klimaat klimaat1 = aKlimaat()
                .withDatumtijd(day.atTime(10, 0))
                .withLuchtvochtigheid(new BigDecimal("20.1"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat1);

        // Lowest humidity on day and earliest
        Klimaat klimaat2 = aKlimaat()
                .withDatumtijd(day.atTime(10, 1))
                .withLuchtvochtigheid(new BigDecimal("20.0"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat2);

        // Lowest humidity on day, but not earliest
        Klimaat klimaat3 = aKlimaat()
                .withDatumtijd(day.atTime(10, 2))
                .withLuchtvochtigheid(new BigDecimal("20.0"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat3);

        Klimaat result = klimaatRepository.earliestLowestHumidityOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    @Test
    void whenGetEarliestHighestHumidityOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor(SENSOR_CODE_GARDEN);

        LocalDate day = LocalDate.of(2022, 1, 1);

        // Not highest humidity
        Klimaat klimaat1 = aKlimaat()
                .withDatumtijd(day.atTime(10, 0))
                .withLuchtvochtigheid(new BigDecimal("19.9"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat1);

        // Highest humidity on day and earliest
        Klimaat klimaat2 = aKlimaat()
                .withDatumtijd(day.atTime(10, 1))
                .withLuchtvochtigheid(new BigDecimal("20.0"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat2);

        // Highest humidity on day, but not earliest
        Klimaat klimaat3 = aKlimaat()
                .withDatumtijd(day.atTime(10, 2))
                .withLuchtvochtigheid(new BigDecimal("20.0"))
                .withKlimaatSensor(klimaatSensor)
                .build();
        entityManager.persist(klimaat3);

        Klimaat result = klimaatRepository.earliestHighestHumidityOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    private KlimaatSensor persistSensor(final String sensorCode) {
        final KlimaatSensor klimaatSensor = KlimaatSensorBuilder.aKlimaatSensor().withCode(sensorCode).build();
        entityManager.persist(klimaatSensor);
        return klimaatSensor;
    }
}
