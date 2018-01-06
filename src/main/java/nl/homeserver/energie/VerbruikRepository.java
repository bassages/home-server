package nl.homeserver.energie;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Transactional
public interface VerbruikRepository extends JpaRepository<Meterstand, Long> {

    // Native queries
    String STROOMVERBRUIK_NORMAAL_TARIEF_IN_PERIOD = "SELECT (MAX(stroom_tarief2) - MIN(stroom_tarief2)) FROM meterstand WHERE date_time >= :van AND date_time < :tot";
    String STROOMVERBRUIK_LAAG_TARIEF_IN_PERIOD = "SELECT (MAX(stroom_tarief1) - MIN(stroom_tarief1)) FROM meterstand WHERE date_time >= :van AND date_time < :tot";
    String GASVERBRUIK_IN_PERIOD = "SELECT MAX(gas) - MIN(gas) FROM meterstand WHERE date_time >= :van AND date_time < :tot";

    @Query(value = STROOMVERBRUIK_NORMAAL_TARIEF_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikNormaalTariefInPeriod(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = STROOMVERBRUIK_LAAG_TARIEF_IN_PERIOD, nativeQuery = true)
    BigDecimal getStroomVerbruikDalTariefInPeriod(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Query(value = GASVERBRUIK_IN_PERIOD, nativeQuery = true)
    BigDecimal getGasVerbruikInPeriod(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);
}
