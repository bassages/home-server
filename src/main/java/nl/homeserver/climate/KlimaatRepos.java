package nl.homeserver.climate;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
interface KlimaatRepos extends JpaRepository<Klimaat, Long> {

    @Query(value = """
         SELECT k
           FROM Klimaat k
          WHERE k.klimaatSensor.code = :sensorCode
            AND k.datumtijd >= :van
            AND k.datumtijd <= :totEnMet
      ORDER BY k.datumtijd
    """)
    List<Klimaat> findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(@Param("sensorCode") String sensorCode,
                                                                             @Param("van") LocalDateTime van,
                                                                             @Param("totEnMet") LocalDateTime totEnMet);

    @Query(value = """
        SELECT datum
          FROM (
                SELECT k.datum AS datum,
                       MAX(k.temperatuur) AS temperatuur
                  FROM klimaat k
            INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
                 WHERE ks.code = :sensorCode
              GROUP BY k.datum
                HAVING k.datum >= :van
                   AND k.datum < :tot
              ORDER BY temperatuur DESC
                 LIMIT :limit
               ) datums
    """, nativeQuery = true)
    List<Date> getPeakHighTemperatureDates(@Param("sensorCode") String sensorCode,
                                           @Param("van") LocalDate van,
                                           @Param("tot") LocalDate tot,
                                           @Param("limit") int limit);

    @Nullable
    @Query(value = """
        SELECT k.*
          FROM klimaat k
    INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
         WHERE k.datum = :day
           AND ks.code = :sensorCode
      ORDER BY k.temperatuur DESC,
               k.datumtijd
         LIMIT 1
    """, nativeQuery = true)
    Klimaat earliestHighestTemperatureOnDay(@Param("sensorCode") String sensorCode,
                                            @Param("day") LocalDate day);

    @Query(value = """
        SELECT datum
          FROM (
                SELECT k.datum AS datum,
                       MIN(k.temperatuur ) AS temperatuur
                  FROM klimaat k
            INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
                 WHERE ks.code = :sensorCode
              GROUP BY k.datum
                HAVING k.datum >= :van
                   AND k.datum < :tot
              ORDER BY temperatuur
                 LIMIT :limit
               ) datums
    """, nativeQuery = true)
    List<Date> getPeakLowTemperatureDates(@Param("sensorCode") String sensorCode,
                                          @Param("van") LocalDate van,
                                          @Param("tot") LocalDate tot,
                                          @Param("limit") int limit);

    @Nullable
    @Query(value = """
        SELECT k.*
          FROM klimaat k
    INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
         WHERE k.datum = :day
           AND ks.code = :sensorCode
      ORDER BY k.temperatuur,
               k.datumtijd
         LIMIT 1
    """, nativeQuery = true)
    Klimaat earliestLowestTemperatureOnDay(@Param("sensorCode") String sensorCode,
                                           @Param("day") LocalDate day);

    @Query(value = """
        SELECT datum
          FROM (
                SELECT k.datum AS datum,
                       MAX(k.luchtvochtigheid) AS luchtvochtigheid
                  FROM klimaat k
            INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
                 WHERE ks.code = :sensorCode
              GROUP BY k.datum
                HAVING k.datum >= :van
                   AND k.datum < :tot
              ORDER BY luchtvochtigheid DESC
                 LIMIT :limit
               ) datums
    """, nativeQuery = true)
    List<Date> getPeakHighHumidityDates(@Param("sensorCode") String sensorCode,
                                        @Param("van") LocalDate van,
                                        @Param("tot") LocalDate tot,
                                        @Param("limit") int limit);

    @Nullable
    @Query(value = """
        SELECT k.*
          FROM klimaat k
    INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
         WHERE k.datum = :day
           AND ks.code = :sensorCode
      ORDER BY k.luchtvochtigheid DESC,
               k.datumtijd
         LIMIT 1
    """, nativeQuery = true)
    Klimaat earliestHighestHumidityOnDay(@Param("sensorCode") String sensorCode,
                                         @Param("day") LocalDate day);

    @Query(value = """
        SELECT datum
          FROM (
                SELECT k.datum AS datum,
                       MIN(k.luchtvochtigheid) AS luchtvochtigheid
                  FROM klimaat k
            INNER JOIN klimaat_sensor ks ON k.klimaat_sensor_id = ks.id
                 WHERE ks.code = :sensorCode
              GROUP BY k.datum
                HAVING k.datum >= :van
                   AND k.datum < :tot
              ORDER BY luchtvochtigheid
                 LIMIT :limit
               ) datums
    """, nativeQuery = true)
    List<Date> getPeakLowHumidityDates(@Param("sensorCode") String sensorCode,
                                       @Param("van") LocalDate van,
                                       @Param("tot") LocalDate tot,
                                       @Param("limit") int limit);

    @Query(value = """
        SELECT k
          FROM Klimaat k
          JOIN KlimaatSensor ks
         WHERE EXTRACT(DAY FROM k.datumtijd) = EXTRACT(DAY FROM :day)
           AND EXTRACT(MONTH FROM k.datumtijd) = EXTRACT(MONTH FROM :day)
           AND EXTRACT(YEAR FROM k.datumtijd) = EXTRACT(YEAR FROM :day)
           AND ks.code = :sensorCode
      ORDER BY k.luchtvochtigheid,
               k.datumtijd
    """)
    List<Klimaat> earliestLowestHumiditiesOnDay(@Param("sensorCode") String sensorCode,
                                                @Param("day") LocalDate day,
                                                Pageable pageable);

    @Nullable
    default Klimaat earliestLowestHumidityOnDay(final String sensorCode, final LocalDate day) {
        return earliestLowestHumiditiesOnDay(sensorCode, day, PageRequest.of(0, 1))
                .stream().findFirst().orElse(null);
    }

    @Nullable
    @Query(value = """
        SELECT AVG(k.temperatuur)
          FROM Klimaat k
         WHERE k.klimaatSensor.code = :sensorCode
           AND k.datumtijd >= :van
           AND k.datumtijd < :tot
    """)
    BigDecimal getAverageTemperatuur(@Param("sensorCode") String sensorCode,
                                     @Param("van") LocalDateTime van,
                                     @Param("tot") LocalDateTime tot);

    @Nullable
    @Query(value = """
        SELECT AVG(k.luchtvochtigheid)
          FROM Klimaat k
         WHERE k.klimaatSensor.code = :sensorCode
           AND k.datumtijd >= :van
           AND k.datumtijd < :tot
    """)
    BigDecimal getAverageLuchtvochtigheid(@Param("sensorCode") String sensorCode,
                                          @Param("van") LocalDateTime van,
                                          @Param("tot") LocalDateTime tot);

    void deleteByKlimaatSensorCode(String klimaatSensorCode);
}
