package nl.wiegman.home.energie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Transactional
public interface MeterstandRepository extends JpaRepository<Meterstand, Long> {

    // JPQL queries
    String MOST_RECENT_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String OLDEST_IN_PERIOD = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(datumtijd) from Meterstand m where m.datumtijd BETWEEN :van AND :totEnMet)";
    String MOST_RECENT = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MAX(mostrecent.datumtijd) FROM Meterstand mostrecent)";
    String OLDEST = "SELECT m FROM Meterstand m WHERE m.datumtijd = (SELECT MIN(oldest.datumtijd) FROM Meterstand oldest)";

    // Native queries
    String STROOMVERBRUIK_NORMAAL_TARIEF_IN_PERIOD = "SELECT (MAX(stroom_tarief2)-MIN(stroom_tarief2)) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String STROOMVERBRUIK_LAAG_TARIEF_IN_PERIOD = "SELECT (MAX(stroom_tarief1)-MIN(stroom_tarief1)) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String GASVERBRUIK_IN_PERIOD = "SELECT MAX(gas)-MIN(gas) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";

    @Query(value = STROOMVERBRUIK_NORMAAL_TARIEF_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikNormaalTariefInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = STROOMVERBRUIK_LAAG_TARIEF_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikDalTariefInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = GASVERBRUIK_IN_PERIOD, nativeQuery = true)
    BigDecimal getGasVerbruikInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = MOST_RECENT)
    Meterstand getMeestRecente();

    @Query(value = OLDEST)
    Meterstand getOudste();

    @Query(value = MOST_RECENT_IN_PERIOD)
    Meterstand getMeestRecenteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = OLDEST_IN_PERIOD)
    Meterstand getOudsteInPeriode(@Param("van") long van, @Param("totEnMet") long totEnMet);

}
