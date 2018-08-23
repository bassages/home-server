package nl.homeserver.energie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface OpgenomenVermogenRepository extends JpaRepository<OpgenomenVermogen, Long> {

    @Query(value = "SELECT ov FROM OpgenomenVermogen ov WHERE ov.datumtijd >= :van AND ov.datumtijd < :tot ORDER BY ov.datumtijd")
    List<OpgenomenVermogen> getOpgenomenVermogen(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = "SELECT ov FROM OpgenomenVermogen ov WHERE ov.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM OpgenomenVermogen mostrecent)")
    OpgenomenVermogen getMostRecent();

    @Query(value = "SELECT date FROM (" +
                   "  SELECT PARSEDATETIME(FORMATDATETIME(datumtijd, 'dd-MM-yyyy'), 'dd-MM-yyyy') AS date, " +
                   "         COUNT(id) AS nr_of_records " +
                   "    FROM opgenomen_vermogen " +
                   "   WHERE datumtijd >= :fromDate " +
                   "     AND datumtijd < :toDate " +
                   "   GROUP BY date " +
                   "     HAVING nr_of_records > :maxNrOfRowsPerDay " +
                   "   ORDER BY nr_of_records DESC " +
                   " )", nativeQuery = true)
    List<Timestamp> findDatesBeforeToDateWithMoreRowsThan(@Param("fromDate") LocalDate fromDate,
                                                          @Param("toDate") LocalDate toDate,
                                                          @Param("maxNrOfRowsPerDay") int maxNrOfRowsPerDay);

    @Query(value = "  SELECT watt " +
                   "    FROM opgenomen_vermogen " +
                   "   WHERE datumtijd >= :fromDate AND datumtijd < :toDate " +
                   "GROUP BY watt " +
                   "ORDER BY COUNT(id) DESC " +
                   "LIMIT 1;", nativeQuery = true)
    int findMostCommonWattInPeriod(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query(value = "SELECT COUNT(id) FROM OpgenomenVermogen o " +
                   " WHERE datumtijd >= :fromDate AND datumtijd < :toDate")
    long countNumberOfRecordsInPeriod(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query(value = "  SELECT watt, COUNT(id) AS numberOfRecords " +
                   "    FROM opgenomen_vermogen " +
                   "   WHERE datumtijd >= :fromDate AND datumtijd < :toDate " +
                   "     AND watt >= :fromWatt AND watt < :toWatt " +
                   "GROUP BY watt", nativeQuery = true)
    List<NumberOfRecordsPerWatt> numberOfRecordsInRange(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate,
                                                        @Param("fromWatt") int fromWatt, @Param("toWatt") int toWatt);

    interface NumberOfRecordsPerWatt {
        long getWatt();
        long getNumberOfRecords();
    }
}
