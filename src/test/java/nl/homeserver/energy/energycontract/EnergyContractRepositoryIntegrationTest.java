package nl.homeserver.energy.energycontract;

import nl.homeserver.RepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;
import static java.time.Month.*;
import static org.assertj.core.api.Assertions.assertThat;

class EnergyContractRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    EnergyContractRepository energyContractRepository;

    @Test
    void givenNoEnergyContractsExistsWhenFindAllInInPeriodThenNothingFound() {
        final LocalDate from = LocalDate.of(2018, NOVEMBER, 23);
        final LocalDate to = LocalDate.of(2018, DECEMBER, 12);

        assertThat(energyContractRepository.findValidInPeriod(from, to)).isEmpty();
    }

    @Test
    void givenMultipleEnergyContractsWhenFindValidInInPeriodThenFound() {
        // given
        final var energyContract2017 = energyContractWithZeroTariff()
                .validFrom(LocalDate.of(2017, JANUARY, 1))
                .validTo(LocalDate.of(2018, JANUARY, 1))
                .build();
        entityManager.persist(energyContract2017);

        final var energyContract2018 = energyContractWithZeroTariff()
                .validFrom(LocalDate.of(2018, JANUARY, 1))
                .validTo(LocalDate.of(2019, JANUARY, 1))
                .build();
        entityManager.persist(energyContract2018);

        final var energyContractFrom2019 = energyContractWithZeroTariff()
                .validFrom(LocalDate.of(2019, JANUARY, 1))
                .validTo(null)
                .build();
        entityManager.persist(energyContractFrom2019);

        // when / then
        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(2016, JANUARY, 1), LocalDate.of(2017, JANUARY, 1)))
                                           .isEmpty();

        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(2017, JANUARY, 1), LocalDate.of(2017, JANUARY, 2)))
                                           .containsExactly(energyContract2017);

        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(2017, DECEMBER, 31), LocalDate.of(2018, JANUARY, 1)))
                                           .containsExactly(energyContract2017);

        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(2017, DECEMBER, 31), LocalDate.of(2018, JANUARY, 2)))
                                           .containsExactly(energyContract2017, energyContract2018);

        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(2019, JANUARY, 1), LocalDate.of(2019, JANUARY, 2)))
                                           .containsExactly(energyContractFrom2019);

        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(2035, JANUARY, 1), LocalDate.of(2035, JANUARY, 2)))
                                           .containsExactly(energyContractFrom2019);

        assertThat(energyContractRepository.findValidInPeriod(LocalDate.of(1900, JANUARY, 1), LocalDate.of(2035, JANUARY, 1)))
                                           .containsExactly(energyContract2017, energyContract2018, energyContractFrom2019);

    }

    @Test
    void givenFutureEnergyContractWhenFindingCurrentAndPeriodThenFutureContractIsOnlyReturnedForFuturePeriod() {
        final LocalDate currentDate = LocalDate.of(2026, NOVEMBER, 4);
        final LocalDate futureDate = LocalDate.of(2027, JANUARY, 1);
        final var currentEnergyContract = energyContractWithZeroTariff()
                .validFrom(LocalDate.of(2026, JANUARY, 1))
                .validTo(futureDate)
                .build();
        entityManager.persist(currentEnergyContract);
        final var futureEnergyContract = energyContractWithZeroTariff()
                .validFrom(futureDate)
                .validTo(null)
                .build();
        entityManager.persist(futureEnergyContract);

        assertThat(energyContractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(currentDate))
                .isEqualTo(currentEnergyContract);
        assertThat(energyContractRepository.findValidInPeriod(currentDate, futureDate))
                .containsExactly(currentEnergyContract);
        assertThat(energyContractRepository.findValidInPeriod(futureDate, futureDate.plusDays(1)))
                .containsExactly(futureEnergyContract);
    }

    EnergyContract.EnergyContractBuilder energyContractWithZeroTariff() {
        return EnergyContract.builder()
                .gasPerCubicMeter(ZERO)
                .electricityPerKwhOffPeakTariff(ZERO)
                .electricityPerKwhStandardTariff(ZERO);
    }
}
