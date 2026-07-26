package nl.homeserver.energy.opgenomenvermogen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Nullable;
import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface OpgenomenVermogenRepository extends JpaRepository<OpgenomenVermogen, Long> {

    @Query(value = """
        SELECT ov
          FROM OpgenomenVermogen ov
         WHERE ov.datumtijd >= :van
           AND ov.datumtijd < :tot
      ORDER BY ov.datumtijd""")
    List<OpgenomenVermogen> getOpgenomenVermogen(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Nullable
    @Query(value = """
        SELECT ov
          FROM OpgenomenVermogen ov
         WHERE ov.datumtijd = (
                SELECT MAX(mostrecent.datumtijd) FROM OpgenomenVermogen mostrecent
            )""")
    OpgenomenVermogen getMostRecent();

    @Nullable
    @Query(value = """
        SELECT ov
          FROM OpgenomenVermogen ov
         WHERE ov.datumtijd = (
                SELECT MIN(mostrecent.datumtijd) FROM OpgenomenVermogen mostrecent
            )""")
    OpgenomenVermogen getOldest();

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Query(value = """
       SELECT datum FROM (
         SELECT datum,
                COUNT(id) AS nr_of_records
           FROM opgenomen_vermogen
          WHERE datumtijd >= :fromDate
            AND datumtijd < :toDate
          GROUP BY datum
            HAVING nr_of_records > :maxNrOfRowsPerDay
          ORDER BY nr_of_records DESC
       )""", nativeQuery = true)
    List<LocalDate> findDatesBeforeToDateWithMoreRowsThan(@Param("fromDate") LocalDate fromDate,
                                                     @Param("toDate") LocalDate toDate,
                                                     @Param("maxNrOfRowsPerDay") int maxNrOfRowsPerDay);

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Nullable
    @Query(value = """
        SELECT active_power_total_in_watts
          FROM opgenomen_vermogen
         WHERE datumtijd >= :fromDate AND datumtijd < :toDate
      GROUP BY active_power_total_in_watts
      ORDER BY COUNT(id) DESC
         LIMIT 1""", nativeQuery = true)
    Integer findMostCommonActivePowerTotalInWattsInPeriod(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query(value = """
        SELECT COUNT(id)
          FROM OpgenomenVermogen
         WHERE datumtijd >= :fromDate
           AND datumtijd < :toDate""")
    long countNumberOfRecordsInPeriod(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
    @Query(value = """
        SELECT active_power_total_in_watts AS activePowerTotalInWatts, COUNT(id) AS numberOfRecords
          FROM opgenomen_vermogen
         WHERE datumtijd >= :fromDateTime AND datumtijd < :toDateTime
           AND active_power_total_in_watts >= :fromActivePowerTotalInWatts AND active_power_total_in_watts < :toActivePowerTotalInWatts
      GROUP BY active_power_total_in_watts""", nativeQuery = true)
    List<NumberOfRecordsPerWatt> numberOfRecordsInRange(@Param("fromDateTime") LocalDateTime fromDateTime,
                                                        @Param("toDateTime") LocalDateTime toDate,
                                                        @Param("fromActivePowerTotalInWatts") int fromActivePowerTotalInWatts,
                                                        @Param("toActivePowerTotalInWatts") int toActivePowerTotalInWatts);
}
