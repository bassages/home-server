package nl.homeserver.energie.meterstand;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // Please note that BETWEEN in HQL is INCLUSIVE!

    @Nullable
    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MAX(mostrecent.dateTime) FROM Meterstand mostrecent)")
    Meterstand getMostRecent();

    @Nullable
    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MIN(oldest.dateTime) FROM Meterstand oldest)")
    Meterstand getOldest();

    @Nullable
    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MAX(dateTime) from Meterstand m where m.dateTime BETWEEN :van AND :totEnMet)")
    Meterstand getMostRecentInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    @Nullable
    @Query(value = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MIN(dateTime) from Meterstand m where m.dateTime BETWEEN :van AND :totEnMet)")
    Meterstand getOldestInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    List<Meterstand> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT date FROM (" +
                   "  SELECT PARSEDATETIME(FORMATDATETIME(date_time, 'dd-MM-yyyy'), 'dd-MM-yyyy') AS date, " +
                   "         COUNT(id) AS nr_of_records " +
                   "    FROM meterstand " +
                   "   WHERE date_time >= :fromDate " +
                   "     AND date_time < :toDate " +
                   "   GROUP BY date " +
                   "     HAVING nr_of_records > :maxNrOfRowsPerDay " +
                   "   ORDER BY nr_of_records DESC " +
                   " )", nativeQuery = true)
    List<Timestamp> findDatesBeforeToDateWithMoreRowsThan(@Param("fromDate") LocalDate fromDate,
                                                          @Param("toDate") LocalDate toDate,
                                                          @Param("maxNrOfRowsPerDay") int maxNrOfRowsPerDay);
}
