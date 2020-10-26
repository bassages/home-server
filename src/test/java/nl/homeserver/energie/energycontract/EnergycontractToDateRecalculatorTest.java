package nl.homeserver.energie.energycontract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergycontractToDateRecalculatorTest {

    @InjectMocks
    EnergycontractToDateRecalculator energycontractToDateRecalculator;

    @Mock
    EnergycontractRepository energycontractRepository;

    @Test
    void givenMultipleEnergiecontractsWithoutValidToWhenRecalculateThenSetAndSaved() {
        final Energycontract energycontract1 = new Energycontract();
        energycontract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));

        final Energycontract energycontract2 = new Energycontract();
        energycontract2.setValidFrom(LocalDate.of(2017, JANUARY, 6));

        final Energycontract energycontract3 = new Energycontract();
        energycontract3.setValidFrom(LocalDate.of(2017, JANUARY, 12));

        when(energycontractRepository.findAll(any(Sort.class))).thenReturn(List.of(energycontract1, energycontract2, energycontract3));

        energycontractToDateRecalculator.recalculate();

        assertThat(energycontract1.getValidTo()).isEqualTo(energycontract2.getValidFrom());
        assertThat(energycontract2.getValidTo()).isEqualTo(energycontract3.getValidFrom());
        assertThat(energycontract3.getValidTo()).isNull();

        verify(energycontractRepository).save(energycontract1);
        verify(energycontractRepository).save(energycontract2);
    }

    @Test
    void givenValidToAreAlreadyValidWhenRecalculateThenNothingSaved() {
        final Energycontract energycontract1 = new Energycontract();
        energycontract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));
        energycontract1.setValidTo(LocalDate.of(2017, JANUARY, 5));

        final Energycontract energycontract2 = new Energycontract();
        energycontract2.setValidFrom(energycontract1.getValidTo());
        energycontract2.setValidTo(LocalDate.of(2017, JANUARY, 14));

        final Energycontract energycontract3 = new Energycontract();
        energycontract3.setValidFrom(energycontract2.getValidTo());
        energycontract3.setValidTo(null);

        when(energycontractRepository.findAll(any(Sort.class))).thenReturn(List.of(energycontract1, energycontract2, energycontract3));

        energycontractToDateRecalculator.recalculate();

        assertThat(energycontract1.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 5));
        assertThat(energycontract2.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 14));
        assertThat(energycontract3.getValidTo()).isNull();

        verify(energycontractRepository, never()).save(any(Energycontract.class));
    }

    @Test
    void givenSingleEnergiecontractsWithValidToDateWhenRecalculateThenValidToIsClearedAndSaved() {
        final Energycontract energycontract = new Energycontract();
        energycontract.setValidTo(LocalDate.of(2017, JANUARY, 1));

        when(energycontractRepository.findAll(any(Sort.class))).thenReturn(List.of(energycontract));

        energycontractToDateRecalculator.recalculate();

        assertThat(energycontract.getValidTo()).isNull();

        verify(energycontractRepository).save(energycontract);
    }
}
