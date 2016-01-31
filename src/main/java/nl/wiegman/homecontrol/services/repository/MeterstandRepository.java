package nl.wiegman.homecontrol.services.repository;

import nl.wiegman.homecontrol.services.model.api.Meterstand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.transaction.Transactional;
import java.util.List;

@RepositoryRestResource(path = "meterstanden", collectionResourceRel = "meterstanden")
@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // JPQL queries
    String ALL_IN_PERIOD_SORTED = "SELECT m FROM Meterstand m WHERE m.datumtijd >= :van AND m.datumtijd < :tot ORDER BY m.datumtijd";
    String MOST_RECENT_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";

    // Native queries
    String STROOMVERBRUIK_IN_PERIOD = "SELECT (MAX(stroom_tarief1)-min(stroom_tarief1)) + (MAX(stroom_tarief2)-MIN(stroom_tarief2)) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String MOST_RECENT = "SELECT * FROM meterstand WHERE datumtijd = (SELECT MAX(datumtijd) FROM meterstand)";
    String OLDEST = "SELECT * FROM meterstand WHERE datumtijd = (SELECT MIN(datumtijd) FROM meterstand)";

    @Query(value = ALL_IN_PERIOD_SORTED)
    List<Meterstand> getMeterstanden(@Param("van") long van, @Param("tot") long tot);

    @Query(value = STROOMVERBRUIK_IN_PERIOD, nativeQuery = true)
    Integer getVerbruikInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = MOST_RECENT, nativeQuery = true)
    Meterstand getMeestRecente();

    @Query(value = OLDEST, nativeQuery = true)
    Meterstand getOudste();

    @Query(value = MOST_RECENT_IN_PERIOD)
    Meterstand getMeestRecenteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);
}
