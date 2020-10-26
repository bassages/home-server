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

class EnergycontractRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    EnergycontractRepository energycontractRepository;

    @Test
    void givenNoEnergieContractsExistsWhenFindAllInInPeriodThenNothingFound() {
        final LocalDate from = LocalDate.of(2018, NOVEMBER, 23);
        final LocalDate to = LocalDate.of(2018, DECEMBER, 12);

        assertThat(energycontractRepository.findValidInPeriod(from, to)).isEmpty();
    }

    @Test
    void givenMultipleEnergycontractsWhenFindValidInInPeriodThenFound() {
        final Energycontract energycontract2017 = anEnergiecontract().withValidFrom(LocalDate.of(2017, JANUARY, 1))
                                                                       .withValidTo(LocalDate.of(2018, JANUARY, 1)).build();
        entityManager.persist(energycontract2017);

        final Energycontract energycontract2018 = anEnergiecontract().withValidFrom(LocalDate.of(2018, JANUARY, 1))
                                                                       .withValidTo(LocalDate.of(2019, JANUARY, 1)).build();
        entityManager.persist(energycontract2018);

        final Energycontract energycontractFrom2019 = anEnergiecontract().withValidFrom(LocalDate.of(2019, JANUARY, 1))
                                                                           .withValidTo(null).build();
        entityManager.persist(energycontractFrom2019);

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(2016, JANUARY, 1), LocalDate.of(2017, JANUARY, 1)))
                                           .isEmpty();

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(2017, JANUARY, 1), LocalDate.of(2017, JANUARY, 2)))
                                           .containsExactly(energycontract2017);

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(2017, DECEMBER, 31), LocalDate.of(2018, JANUARY, 1)))
                                           .containsExactly(energycontract2017);

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(2017, DECEMBER, 31), LocalDate.of(2018, JANUARY, 2)))
                                           .containsExactly(energycontract2017, energycontract2018);

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(2019, JANUARY, 1), LocalDate.of(2019, JANUARY, 2)))
                                           .containsExactly(energycontractFrom2019);

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(2035, JANUARY, 1), LocalDate.of(2035, JANUARY, 2)))
                                           .containsExactly(energycontractFrom2019);

        assertThat(energycontractRepository.findValidInPeriod(LocalDate.of(1900, JANUARY, 1), LocalDate.of(2035, JANUARY, 1)))
                                           .containsExactly(energycontract2017, energycontract2018, energycontractFrom2019);

    }
}
