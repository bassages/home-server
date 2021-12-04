package nl.homeserver.energie.energycontract;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static nl.homeserver.energie.energycontract.EnergiecontractBuilder.anEnergiecontract;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import nl.homeserver.RepositoryIntegrationTest;

class EnergyContractRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    EnergyContractRepository energyContractRepository;

    @Test
    void givenNoEnergieContractsExistsWhenFindAllInInPeriodThenNothingFound() {
        final LocalDate from = LocalDate.of(2018, NOVEMBER, 23);
        final LocalDate to = LocalDate.of(2018, DECEMBER, 12);

        assertThat(energyContractRepository.findValidInPeriod(from, to)).isEmpty();
    }

    @Test
    void givenMultipleEnergycontractsWhenFindValidInInPeriodThenFound() {
        final EnergyContract energyContract2017 = anEnergiecontract().withValidFrom(LocalDate.of(2017, JANUARY, 1))
                                                                       .withValidTo(LocalDate.of(2018, JANUARY, 1)).build();
        entityManager.persist(energyContract2017);

        final EnergyContract energyContract2018 = anEnergiecontract().withValidFrom(LocalDate.of(2018, JANUARY, 1))
                                                                       .withValidTo(LocalDate.of(2019, JANUARY, 1)).build();
        entityManager.persist(energyContract2018);

        final EnergyContract energyContractFrom2019 = anEnergiecontract().withValidFrom(LocalDate.of(2019, JANUARY, 1))
                                                                           .withValidTo(null).build();
        entityManager.persist(energyContractFrom2019);

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
}
