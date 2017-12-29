package nl.wiegman.home.energie;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static nl.wiegman.home.energie.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class OpgenomenVermogenServiceTest {

    private OpgenomenVermogenService opgenomenVermogenService;

    @Mock
    private OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    private CacheService cacheService;

    @Captor
    private ArgumentCaptor<List<OpgenomenVermogen>> deletedOpgenomenVermogenCaptor;

    @Before
    public void setup() {
        Clock clock = Clock.systemDefaultZone();
        createOpgenomenVermogenService(clock);
    }

    private void createOpgenomenVermogenService(Clock clock) {
        opgenomenVermogenService = new OpgenomenVermogenService(opgenomenVermogenRepository, cacheService, clock);
    }

    @Test
    public void shouldClearCacheOnDailyCleanup() {
        opgenomenVermogenService.dailyCleanup();
        verify(cacheService).clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    @Test
    public void shouldCleanupOneDay() {
        LocalDate date = LocalDate.of(2016, 1, 1);

        ArgumentCaptor<LocalDateTime> fromDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        when(opgenomenVermogenRepository.getOpgenomenVermogen(fromDateCaptor.capture(), toDateCaptor.capture())).thenReturn(emptyList());

        opgenomenVermogenService.cleanup(date);

        assertThat(fromDateCaptor.getValue()).isEqualTo(date.atStartOfDay());
        assertThat(toDateCaptor.getValue()).isEqualTo(date.atStartOfDay().plusDays(1));

        verify(opgenomenVermogenRepository).getOpgenomenVermogen(any(), any());
        verifyNoMoreInteractions(opgenomenVermogenRepository);
    }

    @Test
    public void shouldKeepLatestRecordInMinuteWhenWattIsTheSame() {
        LocalDate date = LocalDate.of(2016, 1, 1);

        OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 0)).withWatt(1).build();
        OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 10)).withWatt(1).build();
        OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(any(), any())).thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenService.cleanup(date);

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(opgenomenVermogen1, opgenomenVermogen2);
    }

    @Test
    public void shouldKeepHighestWatt() {
        LocalDate date = LocalDate.of(2016, 1, 1);

        OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 0)).withWatt(3).build();
        OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 10)).withWatt(2).build();
        OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(date.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(any(), any())).thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenService.cleanup(date);

        verify(opgenomenVermogenRepository).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getValue()).containsExactlyInAnyOrder(opgenomenVermogen2, opgenomenVermogen3);
    }

    @Test
    public void shouldCleanUpPerMinuteAndDeletePerHour() {
        LocalDate date = LocalDate.of(2016, 1, 1);

        OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 0, 0)).build();
        OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 0, 10)).build();

        OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 1, 0)).build();
        OpgenomenVermogen opgenomenVermogen4 = aOpgenomenVermogen().withDatumTijd(date.atTime(12, 1, 10)).build();

        OpgenomenVermogen opgenomenVermogen5 = aOpgenomenVermogen().withDatumTijd(date.atTime(13, 1, 0)).build();
        OpgenomenVermogen opgenomenVermogen6 = aOpgenomenVermogen().withDatumTijd(date.atTime(13, 1, 10)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(any(), any())).thenReturn(asList(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3, opgenomenVermogen4, opgenomenVermogen5, opgenomenVermogen6));

        opgenomenVermogenService.cleanup(date);

        verify(opgenomenVermogenRepository, times(2)).deleteInBatch(deletedOpgenomenVermogenCaptor.capture());

        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(0)).containsExactlyInAnyOrder(opgenomenVermogen1, opgenomenVermogen3);
        assertThat(deletedOpgenomenVermogenCaptor.getAllValues().get(1)).containsExactlyInAnyOrder(opgenomenVermogen5);
    }
}