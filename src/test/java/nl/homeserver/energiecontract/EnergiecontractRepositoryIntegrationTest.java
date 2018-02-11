package nl.homeserver.energiecontract;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static nl.homeserver.energie.EnergiecontractBuilder.anEnergiecontract;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource("/integrationtests.properties")
public class EnergiecontractRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EnergiecontractRepository energiecontractRepository;

    @Test
    public void givenNoEnergieContractsExistsWhenFindAllInInPeriodThenNothingFound() {
        LocalDate from = LocalDate.of(2018, NOVEMBER, 23);
        LocalDate to = LocalDate.of(2018, DECEMBER, 12);

        assertThat(energiecontractRepository.findValidInPeriod(from, to)).isEmpty();
    }

    @Test
    public void givenMultipleEnergycontractsWhenFindValidInInPeriodThenFound() {
        Energiecontract energiecontract2017 = anEnergiecontract().withValidFrom(LocalDate.of(2017, JANUARY, 1))
                                                                 .withValidTo(LocalDate.of(2018, JANUARY, 1)).build();
        entityManager.persist(energiecontract2017);

        Energiecontract energiecontract2018 = anEnergiecontract().withValidFrom(LocalDate.of(2018, JANUARY, 1))
                                                                 .withValidTo(LocalDate.of(2019, JANUARY, 1)).build();
        entityManager.persist(energiecontract2018);

        Energiecontract energiecontractFrom2019 = anEnergiecontract().withValidFrom(LocalDate.of(2019, JANUARY, 1))
                                                                     .withValidTo(null).build();
        entityManager.persist(energiecontractFrom2019);

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(2016, JANUARY, 1), LocalDate.of(2017, JANUARY, 1)))
                                            .isEmpty();

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(2017, JANUARY, 1), LocalDate.of(2017, JANUARY, 2)))
                                            .containsExactly(energiecontract2017);

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(2017, DECEMBER, 31), LocalDate.of(2018, JANUARY, 1)))
                                            .containsExactly(energiecontract2017);

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(2017, DECEMBER, 31), LocalDate.of(2018, JANUARY, 2)))
                                           .containsExactly(energiecontract2017, energiecontract2018);

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(2019, JANUARY, 1), LocalDate.of(2019, JANUARY, 2)))
                                            .containsExactly(energiecontractFrom2019);

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(2035, JANUARY, 1), LocalDate.of(2035, JANUARY, 2)))
                                            .containsExactly(energiecontractFrom2019);

        assertThat(energiecontractRepository.findValidInPeriod(LocalDate.of(1900, JANUARY, 1), LocalDate.of(2035, JANUARY, 1)))
                                            .containsExactly(energiecontract2017, energiecontract2018, energiecontractFrom2019);

    }
}