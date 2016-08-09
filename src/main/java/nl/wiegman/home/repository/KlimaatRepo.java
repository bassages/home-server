package nl.wiegman.home.repository;

import nl.wiegman.home.model.Klimaat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Transactional
public interface KlimaatRepo extends JpaRepository<Klimaat, Long> {

    // JPQL queries
    String MOST_RECENT = "SELECT k FROM Klimaat k WHERE k.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM Klimaat mostrecent)";

    // Native queries
    String PEAK_HIGH_TEMPERATURE_DATES = "SELECT datum FROM (SELECT date(datumtijd) AS datum, MAX(temperatuur) AS temperatuur FROM klimaat GROUP BY date(datumtijd) HAVING datum >= :van and datum < :tot ORDER BY temperatuur DESC LIMIT :limit) datums";
    String FIRST_HIGHEST_TEMPERATURE_ON_DAY = "SELECT * FROM klimaat WHERE date(datumtijd) = :date ORDER BY temperatuur DESC, datumtijd ASC LIMIT 1";

    String PEAK_LOW_TEMPERATURE_DATES = "SELECT datum FROM (SELECT date(datumtijd) AS datum, MIN(temperatuur) AS temperatuur FROM klimaat GROUP BY date(datumtijd) HAVING datum >= :van and datum < :tot ORDER BY temperatuur ASC LIMIT :limit) datums";
    String FIRST_LOWEST_TEMPERATURE_ON_DAY = "SELECT * FROM klimaat WHERE date(datumtijd) = :date ORDER BY temperatuur ASC, datumtijd ASC LIMIT 1";

    List<Klimaat> findByDatumtijdBetweenOrderByDatumtijd(@Param("van") Date van, @Param("tot") Date tot);

    @Query(value = MOST_RECENT)
    Klimaat getMostRecent();

    @Query(value = PEAK_HIGH_TEMPERATURE_DATES, nativeQuery = true)
    List<Date> getPeakHighTemperatureDates(@Param("van") Date van, @Param("tot") Date tot, @Param("limit") int limit);

    @Query(value = FIRST_HIGHEST_TEMPERATURE_ON_DAY, nativeQuery = true)
    Klimaat firstHighestTemperatureOnDay(@Param("date") Date day);

    @Query(value = PEAK_LOW_TEMPERATURE_DATES, nativeQuery = true)
    List<Date> getPeakLowTemperatureDates(@Param("van") Date van, @Param("tot") Date tot, @Param("limit") int limit);

    @Query(value = FIRST_LOWEST_TEMPERATURE_ON_DAY, nativeQuery = true)
    Klimaat firstLowestTemperatureOnDay(@Param("date") Date day);

}
