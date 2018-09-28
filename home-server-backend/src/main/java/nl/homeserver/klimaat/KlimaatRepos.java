package nl.homeserver.klimaat;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
interface KlimaatRepos extends JpaRepository<Klimaat, Long> {

    @Query(value = "  SELECT k FROM Klimaat k "
                 + "   WHERE k.klimaatSensor.code = :sensorCode "
                 + "     AND k.datumtijd >= :van "
                 + "     AND k.datumtijd <= :totEnMet "
                 + "ORDER BY k.datumtijd")
    List<Klimaat> findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(@Param("sensorCode") String sensorCode,
                                                                             @Param("van") LocalDateTime van,
                                                                             @Param("totEnMet") LocalDateTime totEnMet);

    @Query(value = "SELECT datum FROM (  SELECT k.datum AS datum, "
                 + "                            MAX(k.temperatuur) AS temperatuur"
                 + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                 + "                      WHERE ks.code = :sensorCode "
                 + "                   GROUP BY k.datum "
                 + "                     HAVING k.datum >= :van AND k.datum < :tot "
                 + "                   ORDER BY temperatuur DESC LIMIT :limit"
                 + "                  ) datums", nativeQuery = true)
    List<Date> getPeakHighTemperatureDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Nullable
    @Query(value = "  SELECT * "
                 + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                 + "   WHERE k.datum = :date "
                 + "     AND ks.code = :sensorCode "
                 + "ORDER BY k.temperatuur DESC, k.datumtijd ASC LIMIT 1", nativeQuery = true)
    Klimaat earliestHighestTemperatureOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Query(value = "SELECT datum FROM (  SELECT k.datum AS datum,"
                 + "                            MIN(k.temperatuur) AS temperatuur"
                 + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id"
                 + "                      WHERE ks.code = :sensorCode"
                 + "                   GROUP BY k.datum"
                 + "                     HAVING k.datum >= :van AND k.datum < :tot"
                 + "                   ORDER BY temperatuur ASC LIMIT :limit"
                 + "                  ) datums", nativeQuery = true)
    List<Date> getPeakLowTemperatureDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Nullable
    @Query(value = "  SELECT * "
                 + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                 + "   WHERE k.datum = :date "
                 + "     AND ks.code = :sensorCode "
                 + "ORDER BY k.temperatuur ASC, k.datumtijd ASC LIMIT 1", nativeQuery = true)
    Klimaat earliestLowestTemperatureOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Query(value = "SELECT datum FROM (  SELECT k.datum AS datum, "
                 + "                            MAX(k.luchtvochtigheid) AS luchtvochtigheid"
                 + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id"
                 + "                      WHERE ks.code = :sensorCode"
                 + "                   GROUP BY k.datum"
                 + "                     HAVING k.datum >= :van AND k.datum < :tot"
                 + "                   ORDER BY luchtvochtigheid DESC "
                 + "                      LIMIT :limit "
                 + "                  ) datums", nativeQuery = true)
    List<Date> getPeakHighHumidityDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Nullable
    @Query(value = "  SELECT * "
                 + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                 + "   WHERE k.datum = :date "
                 + "     AND ks.code = :sensorCode "
                 + "ORDER BY k.luchtvochtigheid DESC, k.datumtijd ASC LIMIT 1", nativeQuery = true)
    Klimaat earliestHighestHumidityOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Nullable
    @Query(value = "SELECT datum FROM (  SELECT k.datum AS datum,"
                 + "                            MIN(k.luchtvochtigheid) AS luchtvochtigheid"
                 + "                       FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id"
                 + "                      WHERE ks.code = :sensorCode"
                 + "                   GROUP BY k.datum"
                 + "                     HAVING k.datum >= :van AND k.datum < :tot"
                 + "                   ORDER BY luchtvochtigheid ASC "
                 + "                      LIMIT :limit "
                 + "                  ) datums", nativeQuery = true)
    List<Date> getPeakLowHumidityDates(@Param("sensorCode") String sensorCode, @Param("van") LocalDate van, @Param("tot") LocalDate tot, @Param("limit") int limit);

    @Nullable
    @Query(value = "  SELECT * "
                 + "    FROM klimaat k INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id "
                 + "   WHERE k.datum = :date "
                 + "     AND ks.code = :sensorCode "
                 + "ORDER BY k.luchtvochtigheid ASC, k.datumtijd ASC "
                 + " LIMIT 1", nativeQuery = true)
    Klimaat earliestLowestHumidityOnDay(@Param("sensorCode") String sensorCode, @Param("date") LocalDate day);

    @Nullable
    @Query(value = "SELECT AVG(k.temperatuur) "
                 + "  FROM Klimaat k "
                 + " WHERE k.klimaatSensor.code = :sensorCode "
                 + "  AND k.datumtijd >= :van AND k.datumtijd < :tot")
    BigDecimal getAverageTemperatuur(@Param("sensorCode") String sensorCode, @Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Nullable
    @Query(value = "SELECT AVG(k.luchtvochtigheid) "
                 + "  FROM Klimaat k "
                 + " WHERE k.klimaatSensor.code = :sensorCode "
                 + "   AND k.datumtijd >= :van AND k.datumtijd < :tot")
    BigDecimal getAverageLuchtvochtigheid(@Param("sensorCode") String sensorCode, @Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    void deleteByKlimaatSensorCode(String klimaatSensorCode);
}
