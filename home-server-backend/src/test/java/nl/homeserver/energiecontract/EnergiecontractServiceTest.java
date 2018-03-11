package nl.homeserver.energiecontract;

import static java.time.Month.APRIL;
import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
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
import org.mockito.runners.MockitoJUnitRunner;

import nl.homeserver.DateTimePeriod;
import nl.homeserver.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class EnergiecontractServiceTest {

    @InjectMocks
    private EnergiecontractService energiecontractService;

    @Mock
    private EnergiecontractToDateRecalculator energiecontractToDateRecalculator;
    @Mock
    private EnergiecontractRepository energiecontractRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private Clock clock;

    @Test
    public void whenGetByIdThenDelegatedToRepository() {
        long id = 132L;
        Energiecontract energiecontract = mock(Energiecontract.class);
        when(energiecontractRepository.getOne(id)).thenReturn(energiecontract);

        assertThat(energiecontractService.getById(id)).isSameAs(energiecontract);
    }

    @Test
    public void whenGetAllThenDelegatedToRepository() {
        List<Energiecontract> all = asList(mock(Energiecontract.class), mock(Energiecontract.class));
        when(energiecontractRepository.findAll()).thenReturn(all);

        assertThat(energiecontractService.getAll()).isSameAs(all);
    }

    @Test
    public void whenGetCurrentThenDelegatedToRepository() {
        LocalDate today = LocalDate.of(2019, JANUARY, 1);
        timeTravelTo(clock, today.atStartOfDay());

        Energiecontract energiecontract = mock(Energiecontract.class);
        when(energiecontractRepository.findFirstByValidFromLessThanEqualOrderByValidFromDesc(today)).thenReturn(energiecontract);

        assertThat(energiecontractService.getCurrent()).isSameAs(energiecontract);
    }

    @Test
    public void whenDeleteThenDeletedFromRepository() {
        long id = 12;
        energiecontractService.delete(id);

        verify(energiecontractRepository).delete(id);
    }

    @Test
    public void whenDeleteThenAllCachesCleared() {
        long id = 12;
        energiecontractService.delete(id);

        verify(cacheService).clearAll();
    }

    @Test
    public void whenDeleteThenValidToRecalculated() {
        long id = 12;
        energiecontractService.delete(id);

        verify(energiecontractToDateRecalculator).recalculate();
    }

    @Test
    public void whenSaveThenSavedByRepository() {
        Energiecontract energiecontract = mock(Energiecontract.class);
        energiecontractService.save(energiecontract);

        verify(energiecontractRepository).save(energiecontract);
    }

    @Test
    public void whenSaveThenAllCachesCleared() {
        Energiecontract energiecontract = mock(Energiecontract.class);
        energiecontractService.save(energiecontract);

        verify(cacheService).clearAll();
    }

    @Test
    public void whenSaveThenValidToRecalculated() {
        Energiecontract energiecontract = mock(Energiecontract.class);
        energiecontractService.save(energiecontract);

        verify(energiecontractToDateRecalculator).recalculate();
    }

    @Test
    public void whenFindAllThenRetrievedFromRepository() {
        LocalDateTime from = LocalDate.of(2018, APRIL, 21).atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        List<Energiecontract> energiecontractsInPeriod = asList(mock(Energiecontract.class), mock(Energiecontract.class));

        when(energiecontractRepository.findValidInPeriod(from.toLocalDate(), to.toLocalDate()))
                                      .thenReturn(energiecontractsInPeriod);

        assertThat(energiecontractService.findAllInInPeriod(period)).isSameAs(energiecontractsInPeriod);
    }
}