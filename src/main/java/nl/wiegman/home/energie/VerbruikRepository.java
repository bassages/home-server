package nl.wiegman.home.energie;

import java.math.BigDecimal;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface VerbruikRepository extends JpaRepository<Meterstand, Long> {

    // Native queries
    String STROOMVERBRUIK_NORMAAL_TARIEF_IN_PERIOD = "SELECT (MAX(stroom_tarief2) - MIN(stroom_tarief2)) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String STROOMVERBRUIK_LAAG_TARIEF_IN_PERIOD = "SELECT (MAX(stroom_tarief1) - MIN(stroom_tarief1)) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";
    String GASVERBRUIK_IN_PERIOD = "SELECT MAX(gas)-MIN(gas) FROM meterstand WHERE datumtijd >= :van AND datumtijd < :totEnMet";

    @Query(value = STROOMVERBRUIK_NORMAAL_TARIEF_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikNormaalTariefInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = STROOMVERBRUIK_LAAG_TARIEF_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikDalTariefInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);

    @Query(value = GASVERBRUIK_IN_PERIOD, nativeQuery = true)
    BigDecimal getGasVerbruikInPeriod(@Param("van") long van, @Param("totEnMet") long totEnMet);
}
