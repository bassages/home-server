package nl.homeserver.energiecontract;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

@RunWith(MockitoJUnitRunner.class)
public class EnergiecontractToDateRecalculatorTest {

    @InjectMocks
    private EnergiecontractToDateRecalculator energiecontractToDateRecalculator;

    @Mock
    private EnergiecontractRepository energiecontractRepository;

    @Test
    public void givenMultipleEnergiecontractsWithoutValidToWhenRecalculateThenSetAndSaved() {
        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setValidFrom(LocalDate.of(2017, JANUARY, 6));

        Energiecontract energiecontract3 = new Energiecontract();
        energiecontract3.setValidFrom(LocalDate.of(2017, JANUARY, 12));

        when(energiecontractRepository.findAll(any(Sort.class))).thenReturn(asList(energiecontract1, energiecontract2, energiecontract3));

        energiecontractToDateRecalculator.recalculate();

        assertThat(energiecontract1.getValidTo()).isEqualTo(energiecontract2.getValidFrom());
        assertThat(energiecontract2.getValidTo()).isEqualTo(energiecontract3.getValidFrom());
        assertThat(energiecontract3.getValidTo()).isNull();

        verify(energiecontractRepository).save(energiecontract1);
        verify(energiecontractRepository).save(energiecontract2);
    }

    @Test
    public void givenValidToAreAlreadyValidWhenRecalculateThenNothingSaved() {
        Energiecontract energiecontract1 = new Energiecontract();
        energiecontract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));
        energiecontract1.setValidTo(LocalDate.of(2017, JANUARY, 5));

        Energiecontract energiecontract2 = new Energiecontract();
        energiecontract2.setValidFrom(energiecontract1.getValidTo());
        energiecontract2.setValidTo(LocalDate.of(2017, JANUARY, 14));

        Energiecontract energiecontract3 = new Energiecontract();
        energiecontract3.setValidFrom(energiecontract2.getValidTo());
        energiecontract3.setValidTo(null);

        when(energiecontractRepository.findAll(any(Sort.class))).thenReturn(asList(energiecontract1, energiecontract2, energiecontract3));

        energiecontractToDateRecalculator.recalculate();

        assertThat(energiecontract1.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 5));
        assertThat(energiecontract2.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 14));
        assertThat(energiecontract3.getValidTo()).isNull();

        verify(energiecontractRepository, never()).save(any(Energiecontract.class));
    }

    @Test
    public void givenSingleEnergiecontractsWithValidToDateWhenRecalculateThenValidToIsClearedAndSaved() {
        Energiecontract energiecontract = new Energiecontract();
        energiecontract.setValidTo(LocalDate.of(2017, JANUARY, 1));

        when(energiecontractRepository.findAll(any(Sort.class))).thenReturn(singletonList(energiecontract));

        energiecontractToDateRecalculator.recalculate();

        assertThat(energiecontract.getValidTo()).isNull();

        verify(energiecontractRepository).save(energiecontract);
    }
}