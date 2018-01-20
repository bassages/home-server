package nl.homeserver.klimaat;

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
    String AVERAGE_LUCHTVOCHTIGHEID_BETWEEN = "SELECT AVG(k.luchtvochtigheid) "
                                            + "  FROM Klimaat k "
                                            + " WHERE k.klimaatSensor.code = :sensorCode "
                                            + "   AND k.datumtijd >= :van AND k.datumtijd < :tot";

    String AVERAGE_TEMPERATUUR_BETWEEEN = "SELECT AVG(k.temperatuur) "
                                        + "  FROM Klimaat k "
                                        + " WHERE k.klimaatSensor.code = :sensorCode "
                                        + "  AND k.datumtijd >= :van AND k.datumtijd < :tot";

    String BY_KLIMAAT_SENSOR_CODE_AND_DATUMTIJD_IN_PERIOD_ORDER_BY_DATUMTIJD = "  SELECT k FROM Klimaat k "
                                                                             + "   WHERE k.klimaatSensor.code = :sensorCode "
                                                                             + "     AND k.datumtijd >= :van "
                                                                             + "     AND k.datumtijd <= :totEnMet "
                                                                             + "ORDER BY k.datumtijd";

    // Native queries
    String PEAK_HIGH_TEMPERATURE_DATES = "SELECT datum FROM (  SELECT k.datum AS datum, "
                                       + "                            MAX(k.temperatuur) AS temperatuur"
                                       + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                                       + "                      WHERE ks.code = :sensorCode "
                                       + "                   GROUP BY k.datum "
                                       + "                     HAVING k.datum >= :van AND k.datum < :tot "
                                       + "                   ORDER BY temperatuur "
                                       + "                   DESC LIMIT :limit "
                                       + "                  ) datums";

    String FIRST_HIGHEST_TEMPERATURE_ON_DAY = "  SELECT * "
                                            + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                                            + "   WHERE k.datum = :date "
                                            + "     AND ks.code = :sensorCode "
                                            + "ORDER BY k.temperatuur DESC, k.datumtijd ASC LIMIT 1";

    String PEAK_LOW_TEMPERATURE_DATES = "SELECT datum FROM (  SELECT k.datum AS datum,"
                                      + "                            MIN(k.temperatuur) AS temperatuur"
                                      + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id"
                                      + "                      WHERE ks.code = :sensorCode"
                                      + "                   GROUP BY k.datum"
                                      + "                     HAVING k.datum >= :van AND k.datum < :tot"
                                      + "                   ORDER BY temperatuur"
                                      + "                   DESC LIMIT :limit"
                                      + "                  ) datums";

    String FIRST_LOWEST_TEMPERATURE_ON_DAY = "  SELECT * "
                                           + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                                           + "   WHERE k.datum = :date "
                                           + "     AND ks.code = :sensorCode "
                                           + "ORDER BY k.temperatuur ASC, k.datumtijd ASC LIMIT 1";

    String PEAK_HIGH_HUMIDITY_DATES = "SELECT datum FROM (  SELECT k.datum AS datum, "
                                    + "                            MAX(k.luchtvochtigheid) AS luchtvochtigheid"
                                    + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id"
                                    + "                      WHERE ks.code = :sensorCode"
                                    + "                   GROUP BY k.datum"
                                    + "                     HAVING k.datum >= :van AND k.datum < :tot"
                                    + "                   ORDER BY luchtvochtigheid"
                                    + "                   DESC LIMIT :limit"
                                    + "                  ) datums";

    String FIRST_HIGHEST_HUMIDITY_ON_DAY = "  SELECT * "
                                         + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                                         + "   WHERE k.datum = :date "
                                         + "     AND ks.code = :sensorCode "
                                         + "ORDER BY k.luchtvochtigheid DESC, k.datumtijd ASC LIMIT 1";

    String PEAK_LOW_HUMIDITY_DATES = "SELECT datum FROM (  SELECT k.datum AS datum,"
                                   + "                            MIN(k.luchtvochtigheid) AS luchtvochtigheid"
                                   + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id"
                                   + "                      WHERE ks.code = :sensorCode"
                                   + "                   GROUP BY k.datum"
                                   + "                     HAVING k.datum >= :van AND k.datum < :tot"
                                   + "                   ORDER BY luchtvochtigheid"
                                   + "                   DESC LIMIT :limit"
                                   + "                  ) datums";

    String FIRST_LOWEST_HUMIDITY_ON_DAY = "  SELECT * "
                                        + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                                        + "   WHERE k.datum = :date "
                                        + "     AND ks.code = :sensorCode "
                                        + "ORDER BY k.luchtvochtigheid ASC, k.datumtijd ASC LIMIT 1";

    @Query(value = BY_KLIMAAT_SENSOR_CODE_AND_DATUMTIJD_IN_PERIOD_ORDER_BY_DATUMTIJD)
    List<Klimaat> findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(@Param("sensorCode") String sensorCode,
                                                                             @Param("van") LocalDateTime van,
                                                                             @Param("totEnMet") LocalDateTime totEnMet);

    @Query(value = PEAK_HIGH_TEMPERATURE_DATES, nativeQuery = true)
    List<Date> getPeakHighTemperatureDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_HIGHEST_TEMPERATURE_ON_DAY, nativeQuery = true)
    Klimaat firstHighestTemperatureOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Query(value = PEAK_LOW_TEMPERATURE_DATES, nativeQuery = true)
    List<Date> getPeakLowTemperatureDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_LOWEST_TEMPERATURE_ON_DAY, nativeQuery = true)
    Klimaat firstLowestTemperatureOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Query(value = PEAK_HIGH_HUMIDITY_DATES, nativeQuery = true)
    List<Date> getPeakHighHumidityDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_HIGHEST_HUMIDITY_ON_DAY, nativeQuery = true)
    Klimaat firstHighestHumidityOnDay(@Param("sensorCode") String sensorCode, @Param("date") Date day);

    @Query(value = PEAK_LOW_HUMIDITY_DATES, nativeQuery = true)
    List<Date> getPeakLowHumidityDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Query(value = FIRST_LOWEST_HUMIDITY_ON_DAY, nativeQuery = true)
    Klimaat firstLowestHumidityOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Query(value = AVERAGE_TEMPERATUUR_BETWEEEN)
    BigDecimal getAverageTemperatuur(@Param("sensorCode") String sensorCode, @Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = AVERAGE_LUCHTVOCHTIGHEID_BETWEEN)
    BigDecimal getAverageLuchtvochtigheid(@Param("sensorCode") String sensorCode, @Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);
}
