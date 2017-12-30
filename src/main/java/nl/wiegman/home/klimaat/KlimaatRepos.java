package nl.wiegman.home.klimaat;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface KlimaatRepos extends JpaRepository<Klimaat, Long> {

    // JPQL queries
    String AVERAGE_LUCHTVOCHTIGHEID_BETWEEN = "SELECT avg(luchtvochtigheid) FROM Klimaat WHERE datumtijd >= :van AND datumtijd < :tot";
    String AVERAGE_TEMPERATUUR_BETWEEEN = "SELECT avg(temperatuur) FROM Klimaat WHERE datumtijd >= :van AND datumtijd < :tot";

    String BY_KLIMAAT_SENSOR_CODE_AND_DATUMTIJD_IN_PERIOD_ORDER_BY_DATUMTIJD = "SELECT k FROM Klimaat k where k.klimaatSensor.code = :klimaatSensorCode AND k.datumtijd >= :van AND k.datumtijd <= :totEnMet ORDER BY k.datumtijd";

    // Native queries
    String PEAK_HIGH_TEMPERATURE_DATES = "SELECT datum FROM (SELECT datum, MAX(temperatuur) AS temperatuur FROM klimaat GROUP BY datum HAVING datum >= :van AND datum < :tot ORDER BY temperatuur DESC LIMIT :limit) datums";
    String FIRST_HIGHEST_TEMPERATURE_ON_DAY = "SELECT * FROM klimaat WHERE datum = :date ORDER BY temperatuur DESC, datumtijd ASC LIMIT 1";

    String PEAK_LOW_TEMPERATURE_DATES = "SELECT datum FROM (SELECT datum, MIN(temperatuur) AS temperatuur FROM klimaat GROUP BY datum HAVING datum >= :van AND datum < :tot ORDER BY temperatuur ASC LIMIT :limit) datums";
    String FIRST_LOWEST_TEMPERATURE_ON_DAY = "SELECT * FROM klimaat WHERE datum = :date ORDER BY temperatuur ASC, datumtijd ASC LIMIT 1";

    String PEAK_HIGH_HUMIDITY_DATES = "SELECT datum FROM (SELECT datum, MAX(luchtvochtigheid) AS luchtvochtigheid FROM klimaat GROUP BY datum HAVING datum >= :van AND datum < :tot ORDER BY luchtvochtigheid DESC LIMIT :limit) datums";
    String FIRST_HIGHEST_HUMIDITY_ON_DAY = "SELECT * FROM klimaat WHERE datum = :date ORDER BY luchtvochtigheid DESC, datumtijd ASC LIMIT 1";

    String PEAK_LOW_HUMIDITY_DATES = "SELECT datum FROM (SELECT datum, MIN(luchtvochtigheid) AS luchtvochtigheid FROM klimaat GROUP BY datum HAVING datum >= :van AND datum < :tot ORDER BY luchtvochtigheid ASC LIMIT :limit) datums";
    String FIRST_LOWEST_HUMIDITY_ON_DAY = "SELECT * FROM klimaat WHERE datum = :date ORDER BY luchtvochtigheid ASC, datumtijd ASC LIMIT 1";

    @Query(value = BY_KLIMAAT_SENSOR_CODE_AND_DATUMTIJD_IN_PERIOD_ORDER_BY_DATUMTIJD)
    List<Klimaat> findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(@Param("klimaatSensorCode") String klimaatSensorCode,
                                                                             @Param("van") LocalDateTime van,
                                                                             @Param("totEnMet") LocalDateTime totEnMet);

    @Query(value = PEAK_HIGH_TEMPERATURE_DATES, nativeQuery = true)
    List<Date> getPeakHighTemperatureDates(@Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_HIGHEST_TEMPERATURE_ON_DAY, nativeQuery = true)
    Klimaat firstHighestTemperatureOnDay(@Param("date") LocalDate day);

    @Query(value = PEAK_LOW_TEMPERATURE_DATES, nativeQuery = true)
    List<Date> getPeakLowTemperatureDates(@Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_LOWEST_TEMPERATURE_ON_DAY, nativeQuery = true)
    Klimaat firstLowestTemperatureOnDay(@Param("date") LocalDate day);

    @Query(value = PEAK_HIGH_HUMIDITY_DATES, nativeQuery = true)
    List<Date> getPeakHighHumidityDates(@Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_HIGHEST_HUMIDITY_ON_DAY, nativeQuery = true)
    Klimaat firstHighestHumidityOnDay(@Param("date") Date day);

    @Query(value = PEAK_LOW_HUMIDITY_DATES, nativeQuery = true)
    List<Date> getPeakLowHumidityDates(@Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_LOWEST_HUMIDITY_ON_DAY, nativeQuery = true)
    Klimaat firstLowestHumidityOnDay(@Param("date") LocalDate day);

    @Query(value = AVERAGE_TEMPERATUUR_BETWEEEN)
    BigDecimal getAverageTemperatuur(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = AVERAGE_LUCHTVOCHTIGHEID_BETWEEN)
    BigDecimal getAverageLuchtvochtigheid(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);
}
