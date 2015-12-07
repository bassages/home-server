package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    String VERBRUIK_IN_PERIOD_QUERY = "SELECT (MAX(stroom_tarief1)-min(stroom_tarief1)) + (MAX(stroom_tarief2)-MIN(stroom_tarief2)) FROM meterstand WHERE datumtijd >= :from AND datumtijd < :to";

    String MOST_RECENT_METERSTAND = "SELECT * FROM meterstand WHERE datumtijd = (SELECT MAX(datumtijd) FROM meterstand)";
    String OLDEST_METERSTAND = "SELECT * FROM meterstand WHERE datumtijd = (SELECT MIN(datumtijd) FROM meterstand)";

    @Query(value = VERBRUIK_IN_PERIOD_QUERY, nativeQuery = true)
    Integer getVerbruikInPeriod(@Param("from") long from, @Param("to") long to);

    @Query(value = MOST_RECENT_METERSTAND, nativeQuery = true)
    Meterstand getMostRecentMeterstand();

    @Query(value = "SELECT m FROM Meterstand m WHERE m.datumtijd >= :from AND m.datumtijd < :to ORDER BY m.datumtijd")
    List<Meterstand> getMeterstanden(@Param("from") long from, @Param("to") long to);

    @Query(value = OLDEST_METERSTAND, nativeQuery = true)
    Meterstand getOldestMeterstand();
}
