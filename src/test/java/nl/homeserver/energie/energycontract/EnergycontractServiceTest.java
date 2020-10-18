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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class EnergycontractServiceTest {

    @InjectMocks
    private EnergycontractService energycontractService;

    @Mock
    private EnergycontractToDateRecalculator energycontractToDateRecalculator;
    @Mock
    private EnergycontractRepository energycontractRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private Clock clock;

    @Test
    public void whenGetByIdThenDelegatedToRepository() {
        final long id = 132L;
        final Energycontract energycontract = mock(Energycontract.class);
        when(energycontractRepository.getOne(id)).thenReturn(energycontract);

        assertThat(energycontractService.getById(id)).isSameAs(energycontract);
    }

    @Test
    public void whenGetAllThenDelegatedToRepository() {
        final List<Energycontract> all = List.of(mock(Energycontract.class), mock(Energycontract.class));
        when(energycontractRepository.findAll()).thenReturn(all);

        assertThat(energycontractService.getAll()).isSameAs(all);
    }

    @Test
    public void whenGetCurrentThenDelegatedToRepository() {
        final LocalDate today = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, today.atStartOfDay());

        final Energycontract energycontract = mock(Energycontract.class);
        when(energycontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(today)).thenReturn(energycontract);

        assertThat(energycontractService.getCurrent()).isSameAs(energycontract);
    }

    @Test
    public void whenDeleteThenDeletedFromRepository() {
        final long id = 12;
        energycontractService.delete(id);

        verify(energycontractRepository).deleteById(id);
    }

    @Test
    public void whenDeleteThenAllCachesCleared() {
        final long id = 12;
        energycontractService.delete(id);

        verify(cacheService).clearAll();
    }

    @Test
    public void whenDeleteThenValidToRecalculated() {
        final long id = 12;
        energycontractService.delete(id);

        verify(energycontractToDateRecalculator).recalculate();
    }

    @Test
    public void whenSaveThenSavedByRepository() {
        final Energycontract energycontract = mock(Energycontract.class);
        energycontractService.save(energycontract);

        verify(energycontractRepository).save(energycontract);
    }

    @Test
    public void whenSaveThenAllCachesCleared() {
        final Energycontract energycontract = mock(Energycontract.class);
        energycontractService.save(energycontract);

        verify(cacheService).clearAll();
    }

    @Test
    public void whenSaveThenValidToRecalculated() {
        final Energycontract energycontract = mock(Energycontract.class);
        energycontractService.save(energycontract);

        verify(energycontractToDateRecalculator).recalculate();
    }

    @Test
    public void whenFindAllThenRetrievedFromRepository() {
        final LocalDateTime from = LocalDate.of(2018, APRIL, 21).atStartOfDay();
        final LocalDateTime to = from.plusDays(1);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final List<Energycontract> energiecontractsInPeriod = List.of(mock(Energycontract.class), mock(Energycontract.class));

        when(energycontractRepository.findValidInPeriod(from.toLocalDate(), to.toLocalDate()))
                                      .thenReturn(energiecontractsInPeriod);

        assertThat(energycontractService.findAllInInPeriod(period)).isSameAs(energiecontractsInPeriod);
    }
}
