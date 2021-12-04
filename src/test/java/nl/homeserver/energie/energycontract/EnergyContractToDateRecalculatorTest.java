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
class EnergyContractToDateRecalculatorTest {

    @InjectMocks
    EnergyContractToDateRecalculator energyContractToDateRecalculator;

    @Mock
    EnergyContractRepository energyContractRepository;

    @Test
    void givenMultipleEnergiecontractsWithoutValidToWhenRecalculateThenSetAndSaved() {
        final EnergyContract energyContract1 = new EnergyContract();
        energyContract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));

        final EnergyContract energyContract2 = new EnergyContract();
        energyContract2.setValidFrom(LocalDate.of(2017, JANUARY, 6));

        final EnergyContract energyContract3 = new EnergyContract();
        energyContract3.setValidFrom(LocalDate.of(2017, JANUARY, 12));

        when(energyContractRepository.findAll(any(Sort.class))).thenReturn(List.of(energyContract1, energyContract2, energyContract3));

        energyContractToDateRecalculator.recalculate();

        assertThat(energyContract1.getValidTo()).isEqualTo(energyContract2.getValidFrom());
        assertThat(energyContract2.getValidTo()).isEqualTo(energyContract3.getValidFrom());
        assertThat(energyContract3.getValidTo()).isNull();

        verify(energyContractRepository).save(energyContract1);
        verify(energyContractRepository).save(energyContract2);
    }

    @Test
    void givenValidToAreAlreadyValidWhenRecalculateThenNothingSaved() {
        final EnergyContract energyContract1 = new EnergyContract();
        energyContract1.setValidFrom(LocalDate.of(2017, JANUARY, 1));
        energyContract1.setValidTo(LocalDate.of(2017, JANUARY, 5));

        final EnergyContract energyContract2 = new EnergyContract();
        energyContract2.setValidFrom(energyContract1.getValidTo());
        energyContract2.setValidTo(LocalDate.of(2017, JANUARY, 14));

        final EnergyContract energyContract3 = new EnergyContract();
        energyContract3.setValidFrom(energyContract2.getValidTo());
        energyContract3.setValidTo(null);

        when(energyContractRepository.findAll(any(Sort.class))).thenReturn(List.of(energyContract1, energyContract2, energyContract3));

        energyContractToDateRecalculator.recalculate();

        assertThat(energyContract1.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 5));
        assertThat(energyContract2.getValidTo()).isEqualTo(LocalDate.of(2017, JANUARY, 14));
        assertThat(energyContract3.getValidTo()).isNull();

        verify(energyContractRepository, never()).save(any(EnergyContract.class));
    }

    @Test
    void givenSingleEnergiecontractsWithValidToDateWhenRecalculateThenValidToIsClearedAndSaved() {
        final EnergyContract energyContract = new EnergyContract();
        energyContract.setValidTo(LocalDate.of(2017, JANUARY, 1));

        when(energyContractRepository.findAll(any(Sort.class))).thenReturn(List.of(energyContract));

        energyContractToDateRecalculator.recalculate();

        assertThat(energyContract.getValidTo()).isNull();

        verify(energyContractRepository).save(energyContract);
    }
}
