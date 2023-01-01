package nl.homeserver.energy.energycontract;

import nl.homeserver.DatePeriod;
import nl.homeserver.cache.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.APRIL;
import static java.time.Month.JANUARY;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergyContractServiceTest {

    @InjectMocks
    EnergyContractService energyContractService;

    @Mock
    EnergyContractToDateRecalculator energyContractToDateRecalculator;
    @Mock
    EnergyContractRepository energyContractRepository;
    @Mock
    CacheService cacheService;
    @Mock
    Clock clock;

    @Test
    void whenGetByIdThenDelegatedToRepository() {
        final long id = 132L;
        final EnergyContract energyContract = mock(EnergyContract.class);
        when(energyContractRepository.findById(id)).thenReturn(Optional.of(energyContract));

        assertThat(energyContractService.getById(id)).isSameAs(energyContract);
    }

    @Test
    void whenGetAllThenDelegatedToRepository() {
        final List<EnergyContract> all = List.of(mock(EnergyContract.class), mock(EnergyContract.class));
        when(energyContractRepository.findAll()).thenReturn(all);

        assertThat(energyContractService.getAll()).isSameAs(all);
    }

    @Test
    void whenGetCurrentThenDelegatedToRepository() {
        final LocalDate today = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, today.atStartOfDay());

        final EnergyContract energyContract = mock(EnergyContract.class);
        when(energyContractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(today)).thenReturn(energyContract);

        assertThat(energyContractService.getCurrent()).isSameAs(energyContract);
    }

    @Test
    void whenDeleteThenDeletedFromRepository() {
        final long id = 12;
        energyContractService.delete(id);

        verify(energyContractRepository).deleteById(id);
    }

    @Test
    void whenDeleteThenAllCachesCleared() {
        final long id = 12;
        energyContractService.delete(id);

        verify(cacheService).clearAll();
    }

    @Test
    void whenDeleteThenValidToRecalculated() {
        final long id = 12;
        energyContractService.delete(id);

        verify(energyContractToDateRecalculator).recalculate();
    }

    @Test
    void whenSaveThenSavedByRepository() {
        final EnergyContract energyContract = mock(EnergyContract.class);
        energyContractService.save(energyContract);

        verify(energyContractRepository).save(energyContract);
    }

    @Test
    void whenSaveThenAllCachesCleared() {
        final EnergyContract energyContract = mock(EnergyContract.class);
        energyContractService.save(energyContract);

        verify(cacheService).clearAll();
    }

    @Test
    void whenSaveThenValidToRecalculated() {
        final EnergyContract energyContract = mock(EnergyContract.class);
        energyContractService.save(energyContract);

        verify(energyContractToDateRecalculator).recalculate();
    }

    @Test
    void whenFindAllThenRetrievedFromRepository() {
        final LocalDate from = LocalDate.of(2018, APRIL, 21);
        final LocalDate to = from.plusDays(1);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final List<EnergyContract> energyContractsInPeriod = List.of(mock(EnergyContract.class), mock(EnergyContract.class));

        when(energyContractRepository.findValidInPeriod(from, to))
                                      .thenReturn(energyContractsInPeriod);

        assertThat(energyContractService.findAllInInPeriod(period)).isSameAs(energyContractsInPeriod);
    }
}
