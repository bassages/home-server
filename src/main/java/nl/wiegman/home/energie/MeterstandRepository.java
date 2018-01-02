package nl.wiegman.home.energie;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // JPQL queries. Please note that BETWEEN in HQL is INCLUSIVE
    String MOST_RECENT_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MAX(dateTime) from Meterstand m where m.dateTime BETWEEN :van AND :totEnMet)";
    String OLDEST_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MIN(dateTime) from Meterstand m where m.dateTime BETWEEN :van AND :totEnMet)";
    String MOST_RECENT = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MAX(mostrecent.dateTime) FROM Meterstand mostrecent)";
    String OLDEST = "SELECT m FROM Meterstand m WHERE m.dateTime = (SELECT MIN(oldest.dateTime  ) FROM Meterstand oldest)";

    @Query(value = MOST_RECENT)
    Meterstand getMostRecent();

    @Query(value = OLDEST)
    Meterstand getOldest();

    @Query(value = MOST_RECENT_IN_PERIOD)
    Meterstand getMostRecentInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    @Query(value = OLDEST_IN_PERIOD)
    Meterstand getOldestInPeriod(@Param("van") LocalDateTime start, @Param("totEnMet") LocalDateTime end);

    List<Meterstand> findByDatumtijdBetween(LocalDateTime start, LocalDateTime end);
}
