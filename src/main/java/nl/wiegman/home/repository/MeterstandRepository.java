package nl.wiegman.home.repository;

import nl.wiegman.home.model.Meterstand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // JPQL queries
    String ALL_IN_PERIOD_SORTED = "SELECT m FROM Meterstand m WHERE m.datumtijd >= :van AND m.datumtijd < :tot ORDER BY m.datumtijd";
    String MOST_RECENT_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String OLDEST_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";

    // Native queries
    String STROOMVERBRUIK_IN_PERIOD = "SELECT (MAX(stroom_tarief1)-MIN(stroom_tarief1)) + (MAX(stroom_tarief2)-MIN(stroom_tarief2)) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String GASVERBRUIK_IN_PERIOD = "SELECT MAX(gas)-MIN(gas) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String MOST_RECENT = "SELECT * FROM meterstand WHERE datumtijd = (SELECT MAX(datumtijd) FROM meterstand)";
    String OLDEST = "SELECT * FROM meterstand WHERE datumtijd = (SELECT MIN(datumtijd) FROM meterstand)";

    @Query(value = ALL_IN_PERIOD_SORTED)
    List<Meterstand> getMeterstanden(@Param("van") long van, @Param("tot") long tot);

    @Query(value = STROOMVERBRUIK_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = GASVERBRUIK_IN_PERIOD, nativeQuery = true)
    BigDecimal getGasVerbruikInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = MOST_RECENT, nativeQuery = true)
    Meterstand getMeestRecente();

    @Query(value = OLDEST, nativeQuery = true)
    Meterstand getOudste();

    @Query(value = MOST_RECENT_IN_PERIOD)
    Meterstand getMeestRecenteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = OLDEST_IN_PERIOD)
    Meterstand getOudsteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);

    Meterstand findByDatumtijd(long datumtijd);
}