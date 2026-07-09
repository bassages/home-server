package nl.homeserver.energy.verbruikkosten;

import nl.homeserver.RepositoryIntegrationTest;
import static nl.homeserver.energy.meterreading.MeterstandBuilder.aMeterstand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VerbruikRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    VerbruikRepository verbruikRepository;

    @Test
    void givenMultipleMetersWhenGetGasVerbruikInPeriodThenSumsDeltaPerMeter() {
        // given
        final LocalDateTime from = LocalDate.of(2026, 7, 6).atStartOfDay();
        final LocalDateTime to = from.plusDays(1);

        entityManager.persist(aMeterstand().withDateTime(from.plusHours(1))
                .withStroomTarief1(new BigDecimal("10.000")).withStroomTarief2(new BigDecimal("20.000")).withGas(new BigDecimal("100.000"))
                .withMeterIdentificatieStroom("ELEC_OLD").withMeterIdentificatieGas("GAS_OLD").build());
        entityManager.persist(aMeterstand().withDateTime(from.plusHours(2))
                .withStroomTarief1(new BigDecimal("11.000")).withStroomTarief2(new BigDecimal("21.000")).withGas(new BigDecimal("102.000"))
                .withMeterIdentificatieStroom("ELEC_OLD").withMeterIdentificatieGas("GAS_OLD").build());

        entityManager.persist(aMeterstand().withDateTime(from.plusHours(10))
                .withStroomTarief1(new BigDecimal("0.100")).withStroomTarief2(new BigDecimal("0.200")).withGas(new BigDecimal("0.300"))
                .withMeterIdentificatieStroom("ELEC_NEW").withMeterIdentificatieGas("GAS_NEW").build());
        entityManager.persist(aMeterstand().withDateTime(from.plusHours(11))
                .withStroomTarief1(new BigDecimal("0.600")).withStroomTarief2(new BigDecimal("0.900")).withGas(new BigDecimal("1.800"))
                .withMeterIdentificatieStroom("ELEC_NEW").withMeterIdentificatieGas("GAS_NEW").build());

        // when
        final BigDecimal gasVerbruik = verbruikRepository.getGasVerbruikInPeriod(from, to);

        // then
        assertThat(gasVerbruik).isEqualByComparingTo("3.500");
    }

    @Test
    void givenMultipleMetersWhenGetStroomVerbruikInPeriodThenSumsDeltaPerMeter() {
        // given
        final LocalDateTime from = LocalDate.of(2026, 7, 6).atStartOfDay();
        final LocalDateTime to = from.plusDays(1);

        entityManager.persist(aMeterstand().withDateTime(from.plusHours(1))
                .withStroomTarief1(new BigDecimal("100.000")).withStroomTarief2(new BigDecimal("200.000")).withGas(new BigDecimal("100.000"))
                .withMeterIdentificatieStroom("ELEC_OLD").withMeterIdentificatieGas("GAS_OLD").build());
        entityManager.persist(aMeterstand().withDateTime(from.plusHours(2))
                .withStroomTarief1(new BigDecimal("101.500")).withStroomTarief2(new BigDecimal("202.500")).withGas(new BigDecimal("101.000"))
                .withMeterIdentificatieStroom("ELEC_OLD").withMeterIdentificatieGas("GAS_OLD").build());

        entityManager.persist(aMeterstand().withDateTime(from.plusHours(10))
                .withStroomTarief1(new BigDecimal("0.250")).withStroomTarief2(new BigDecimal("0.500")).withGas(new BigDecimal("0.300"))
                .withMeterIdentificatieStroom("ELEC_NEW").withMeterIdentificatieGas("GAS_NEW").build());
        entityManager.persist(aMeterstand().withDateTime(from.plusHours(11))
                .withStroomTarief1(new BigDecimal("0.750")).withStroomTarief2(new BigDecimal("1.100")).withGas(new BigDecimal("1.800"))
                .withMeterIdentificatieStroom("ELEC_NEW").withMeterIdentificatieGas("GAS_NEW").build());

        // when
        final BigDecimal dalVerbruik = verbruikRepository.getStroomVerbruikDalTariefInPeriod(from, to);
        final BigDecimal normaalVerbruik = verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(from, to);

        // then
        assertThat(dalVerbruik).isEqualByComparingTo("2.000");
        assertThat(normaalVerbruik).isEqualByComparingTo("3.100");
    }
}
