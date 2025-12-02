package nl.homeserver.energy.meterreading;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // Please note that BETWEEN in HQL is INCLUSIVE!

    @Query(value = """
        SELECT m
          FROM Meterstand m
         WHERE m.dateTime = (
                             SELECT MAX(mostrecent.dateTime)
                               FROM Meterstand mostrecent
                            )
        """)
    Optional<Meterstand> getMostRecent();

    @Query(value = """
        SELECT m
          FROM Meterstand m
         WHERE m.dateTime = (
                             SELECT MIN(oldest.dateTime)
                               FROM Meterstand oldest
                            )
        """)
    Optional<Meterstand> getOldest();

    @Query(value = """
        SELECT m
          FROM Meterstand m
         WHERE m.dateTime = (
                             SELECT MAX(m1.dateTime)
                               FROM Meterstand m1
                              WHERE m1.dateTime BETWEEN :van AND :totEnMet
                            )
        """)
    Optional<Meterstand> findMostRecentInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    @Query(value = """
        SELECT m
          FROM Meterstand m
         WHERE m.dateTime = (
                             SELECT MIN(m1.dateTime)
                               FROM Meterstand m1
                              WHERE m1.dateTime BETWEEN :van AND :totEnMet
                            )
        """)
    Optional<Meterstand> findOldestInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    List<Meterstand> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = """
       SELECT date FROM (
         SELECT date,
                COUNT(id) AS nr_of_records
           FROM meterstand
          WHERE date_time >= :fromDate
            AND date_time < :toDate
       GROUP BY date
         HAVING nr_of_records > :maxNrOfRowsPerDay
       ORDER BY nr_of_records DESC
        )""", nativeQuery = true)
    List<LocalDate> findDatesBeforeToDateWithMoreRowsThan(@Param("fromDate") LocalDate fromDate,
                                                     @Param("toDate") LocalDate toDate,
                                                     @Param("maxNrOfRowsPerDay") int maxNrOfRowsPerDay);
}
