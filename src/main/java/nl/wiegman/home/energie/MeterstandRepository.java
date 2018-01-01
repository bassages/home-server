package nl.wiegman.home.energie;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // JPQL queries. Please note that BETWEEN in HQL is INCLUSIVE
    String MOST_RECENT_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String OLDEST_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String MOST_RECENT = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM Meterstand mostrecent)";
    String OLDEST = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(oldest.datumtijd) FROM Meterstand oldest)";

    @Query(value = MOST_RECENT)
    Meterstand getMostRecent();

    @Query(value = OLDEST)
    Meterstand getOldest();

    @Query(value = MOST_RECENT_IN_PERIOD)
    Meterstand getMostRecentInPeriod(@Param("van") long start, @Param("totEnMet") long end);

    @Query(value = OLDEST_IN_PERIOD)
    Meterstand getOldestInPeriod(@Param("van") long start, @Param("totEnMet") long end);

    List<Meterstand> findByDatumtijdBetween(long start, long end);
}
