package nl.homeserver.energie.energycontract;

import static java.time.Month.APRIL;
import static java.time.Month.JANUARY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnergycontractServiceTest {

    @InjectMocks
    EnergycontractService energycontractService;

    @Mock
    EnergycontractToDateRecalculator energycontractToDateRecalculator;
    @Mock
    EnergycontractRepository energycontractRepository;
    @Mock
    CacheService cacheService;
    @Mock
    Clock clock;

    @Test
    void whenGetByIdThenDelegatedToRepository() {
        final long id = 132L;
        final Energycontract energycontract = mock(Energycontract.class);
        when(energycontractRepository.getOne(id)).thenReturn(energycontract);

        assertThat(energycontractService.getById(id)).isSameAs(energycontract);
    }

    @Test
    void whenGetAllThenDelegatedToRepository() {
        final List<Energycontract> all = List.of(mock(Energycontract.class), mock(Energycontract.class));
        when(energycontractRepository.findAll()).thenReturn(all);

        assertThat(energycontractService.getAll()).isSameAs(all);
    }

    @Test
    void whenGetCurrentThenDelegatedToRepository() {
        final LocalDate today = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, today.atStartOfDay());

        final Energycontract energycontract = mock(Energycontract.class);
        when(energycontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(today)).thenReturn(energycontract);

        assertThat(energycontractService.getCurrent()).isSameAs(energycontract);
    }

    @Test
    void whenDeleteThenDeletedFromRepository() {
        final long id = 12;
        energycontractService.delete(id);

        verify(energycontractRepository).deleteById(id);
    }

    @Test
    void whenDeleteThenAllCachesCleared() {
        final long id = 12;
        energycontractService.delete(id);

        verify(cacheService).clearAll();
    }

    @Test
    void whenDeleteThenValidToRecalculated() {
        final long id = 12;
        energycontractService.delete(id);

        verify(energycontractToDateRecalculator).recalculate();
    }

    @Test
    void whenSaveThenSavedByRepository() {
        final Energycontract energycontract = mock(Energycontract.class);
        energycontractService.save(energycontract);

        verify(energycontractRepository).save(energycontract);
    }

    @Test
    void whenSaveThenAllCachesCleared() {
        final Energycontract energycontract = mock(Energycontract.class);
        energycontractService.save(energycontract);

        verify(cacheService).clearAll();
    }

    @Test
    void whenSaveThenValidToRecalculated() {
        final Energycontract energycontract = mock(Energycontract.class);
        energycontractService.save(energycontract);

        verify(energycontractToDateRecalculator).recalculate();
    }

    @Test
    void whenFindAllThenRetrievedFromRepository() {
        final LocalDateTime from = LocalDate.of(2018, APRIL, 21).atStartOfDay();
        final LocalDateTime to = from.plusDays(1);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final List<Energycontract> energiecontractsInPeriod = List.of(mock(Energycontract.class), mock(Energycontract.class));

        when(energycontractRepository.findValidInPeriod(from.toLocalDate(), to.toLocalDate()))
                                      .thenReturn(energiecontractsInPeriod);

        assertThat(energycontractService.findAllInInPeriod(period)).isSameAs(energiecontractsInPeriod);
    }
}
