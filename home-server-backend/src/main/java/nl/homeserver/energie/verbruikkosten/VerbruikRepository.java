package nl.homeserver.energie.verbruikkosten;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nullable;
import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nl.homeserver.energie.meterstand.Meterstand;

@Transactional
interface VerbruikRepository extends JpaRepository<Meterstand, Long> {

    @Nullable
    @Query(value = "SELECT (MAX(stroom_tarief2) - MIN(stroom_tarief2)) FROM meterstand WHERE date_time >= :van AND date_time < :tot", nativeQuery = true)
    BigDecimal getStroomVerbruikNormaalTariefInPeriod(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Nullable
    @Query(value = "SELECT (MAX(stroom_tarief1) - MIN(stroom_tarief1)) FROM meterstand WHERE date_time >= :van AND date_time < :tot", nativeQuery = true)
    BigDecimal getStroomVerbruikDalTariefInPeriod(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);

    @Nullable
    @Query(value = "SELECT MAX(gas) - MIN(gas) FROM meterstand WHERE date_time >= :van AND date_time < :tot", nativeQuery = true)
    BigDecimal getGasVerbruikInPeriod(@Param("van") LocalDateTime van, @Param("tot") LocalDateTime tot);
}
