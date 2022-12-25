package nl.homeserver.climate;

import static nl.homeserver.climate.Klimaat.aKlimaat;
import static nl.homeserver.climate.KlimaatSensor.aKlimaatSensor;
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
        final KlimaatSensor klimaatSensor = persistSensor();

        entityManager.persist(aKlimaat().datumtijd(LocalDateTime.now().minusMinutes(15)).klimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().datumtijd(LocalDateTime.now().minusMinutes(10)).klimaatSensor(klimaatSensor).build());
        entityManager.persist(aKlimaat().datumtijd(LocalDateTime.now().minusMinutes(5)).klimaatSensor(klimaatSensor).build());

        assertThat(klimaatRepository.findAll()).hasSize(3);

        klimaatRepository.deleteByKlimaatSensorCode(SENSOR_CODE_GARDEN);

        assertThat(klimaatRepository.findAll()).isEmpty();
    }

    @Test
    void whenGetEarliestLowestTemperatureOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor();

        final LocalDate day = LocalDate.of(2022, 1, 1);

        // Not the lowest temperature
        final Klimaat klimaat1 = aKlimaat().datumtijd(day.atTime(10, 0))
                                           .temperatuur(new BigDecimal("20.01"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat1);

        // Lowest temperature on day and earliest
        final Klimaat klimaat2 = aKlimaat().datumtijd(day.atTime(10, 1))
                                           .temperatuur(new BigDecimal("20.00"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat2);

        // Lowest temperature on day, but not earliest
        final Klimaat klimaat3 = aKlimaat().datumtijd(day.atTime(10, 2))
                                           .temperatuur(new BigDecimal("20.00"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat3);

        final Klimaat result = klimaatRepository.earliestLowestTemperatureOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    @Test
    void whenGetEarliestHighestTemperatureOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor();

        final LocalDate day = LocalDate.of(2022, 1, 1);

        // Not the highest temperature
        final Klimaat klimaat1 = aKlimaat().datumtijd(day.atTime(10, 0))
                                           .temperatuur(new BigDecimal("19.99"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat1);

        // Highest temperature on day and earliest
        final Klimaat klimaat2 = aKlimaat().datumtijd(day.atTime(10, 1))
                                           .temperatuur(new BigDecimal("20.00"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat2);

        // Highest temperature on day, but not earliest
        final Klimaat klimaat3 = aKlimaat().datumtijd(day.atTime(10, 2))
                                           .temperatuur(new BigDecimal("20.00"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat3);

        final Klimaat result = klimaatRepository.earliestHighestTemperatureOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    @Test
    void givenNoDataExistsWhenGetEarliestLowestHumidityOnDayThenNoneFound() {
        // given no data exists in the repository

        // when
        final Klimaat result = klimaatRepository.earliestLowestHumidityOnDay(
                SENSOR_CODE_GARDEN, LocalDate.of(2022, 1, 1));

        // then
        assertThat(result).isNull();
    }

    @Test
    void givenNoDataExistsWhenGetEarliestLowestHumidityOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor();

        final LocalDate day = LocalDate.of(2022, 1, 1);

        // Not the lowest humidity
        final Klimaat klimaat1 = aKlimaat().datumtijd(day.atTime(10, 0))
                                           .luchtvochtigheid(new BigDecimal("20.1"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat1);

        // Lowest humidity on day and earliest
        final Klimaat klimaat2 = aKlimaat().datumtijd(day.atTime(10, 1))
                                           .luchtvochtigheid(new BigDecimal("20.0"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat2);

        // Lowest humidity on day, but not earliest
        final Klimaat klimaat3 = aKlimaat().datumtijd(day.atTime(10, 2))
                                           .luchtvochtigheid(new BigDecimal("20.0"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat3);

        final Klimaat result = klimaatRepository.earliestLowestHumidityOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    @Test
    void whenGetEarliestHighestHumidityOnDayThenFound() {
        final KlimaatSensor klimaatSensor = persistSensor();

        final LocalDate day = LocalDate.of(2022, 1, 1);

        // Not the highest humidity
        final Klimaat klimaat1 = aKlimaat().datumtijd(day.atTime(10, 0))
                                           .luchtvochtigheid(new BigDecimal("19.9"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat1);

        // Highest humidity on day and earliest
        final Klimaat klimaat2 = aKlimaat().datumtijd(day.atTime(10, 1))
                                           .luchtvochtigheid(new BigDecimal("20.0"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat2);

        // Highest humidity on day, but not earliest
        final Klimaat klimaat3 = aKlimaat().datumtijd(day.atTime(10, 2))
                                           .luchtvochtigheid(new BigDecimal("20.0"))
                                           .klimaatSensor(klimaatSensor)
                                           .build();
        entityManager.persist(klimaat3);

        final Klimaat result = klimaatRepository.earliestHighestHumidityOnDay(SENSOR_CODE_GARDEN, day);

        assertThat(result).isEqualTo(klimaat2);
    }

    private KlimaatSensor persistSensor() {
        final KlimaatSensor klimaatSensor = aKlimaatSensor().code(SENSOR_CODE_GARDEN).build();
        entityManager.persist(klimaatSensor);
        return klimaatSensor;
    }
}
