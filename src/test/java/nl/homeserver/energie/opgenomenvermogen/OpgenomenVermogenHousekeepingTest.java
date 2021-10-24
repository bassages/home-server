package nl.homeserver.energie.opgenomenvermogen;

import ch.qos.logback.classic.spi.LoggingEvent;
import nl.homeserver.CaptureLogging;
import nl.homeserver.ContainsMessageAtLevel;
import nl.homeserver.cache.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static ch.qos.logback.classic.Level.DEBUG;
import static java.time.Month.JANUARY;
import static nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpgenomenVermogenHousekeepingTest {

    @InjectMocks
    OpgenomenVermogenHousekeeping opgenomenVermogenHousekeeping;

    @Mock
    OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    CacheService cacheService;
    @Mock
    Clock clock;

    @Test
    void whenDailyCleanupThenCacheCleared() {
        useSystemDefaultClock(clock);

        opgenomenVermogenHousekeeping.start();

        verify(cacheService).clear(OpgenomenVermogenService.CACHE_NAME_OPGENOMEN_VERMOGEN_HISTORY);
    }

    @Test
    void whenStartThenPastMonthIsCleanedUp() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).findDatesBeforeToDateWithMoreRowsThan(currentDateTime.toLocalDate().minusMonths(1),
                currentDateTime.toLocalDate(), 1440);
    }

    @Test
    void givengivenMultipleOpgenomenVermogenInOneMinuteWithSameWattWhenCleanupThenMostRecentInMinuteIsKept() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withId(1).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).withWatt(1).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withId(2).withDatumTijd(dayToCleanup.atTime(0, 0, 10)).withWatt(1).build();
        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withId(3).withDatumTijd(dayToCleanup.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(List.of(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen1.getId());
        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen2.getId());
    }

    @Test
    void givenMultipleOpgenomenVermogenInOneMinuteWithDifferentWattWhenCleanupThenHighestWattIsKept() {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen opgenomenVermogen1 = aOpgenomenVermogen().withId(1).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).withWatt(3).build();
        final OpgenomenVermogen opgenomenVermogen2 = aOpgenomenVermogen().withId(2).withDatumTijd(dayToCleanup.atTime(0, 0, 10)).withWatt(2).build();
        final OpgenomenVermogen opgenomenVermogen3 = aOpgenomenVermogen().withId(3).withDatumTijd(dayToCleanup.atTime(0, 0, 20)).withWatt(1).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                                        .thenReturn(List.of(opgenomenVermogen1, opgenomenVermogen2, opgenomenVermogen3));

        opgenomenVermogenHousekeeping.start();

        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen2.getId());
        verify(opgenomenVermogenRepository).deleteById(opgenomenVermogen3.getId());
    }

    @CaptureLogging(OpgenomenVermogenHousekeeping.class)
    @Test
    void whenCleanupThenKeptAndDeletedOpgenomensAreLogged(final ArgumentCaptor<LoggingEvent> loggerEventCaptor) {
        final LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        final LocalDateTime currentDateTime = dayToCleanup.plusDays(1).atStartOfDay();
        timeTravelTo(clock, currentDateTime);

        final Timestamp dayToCleanupAsTimeStamp = mock(Timestamp.class);
        when(dayToCleanupAsTimeStamp.toLocalDateTime()).thenReturn(dayToCleanup.atStartOfDay());
        when(opgenomenVermogenRepository.findDatesBeforeToDateWithMoreRowsThan(any(), any(), anyInt()))
                                        .thenReturn(List.of(dayToCleanupAsTimeStamp));

        final OpgenomenVermogen deleted = aOpgenomenVermogen().withId(1L).withDatumTijd(dayToCleanup.atTime(0, 0, 0)).build();
        final OpgenomenVermogen kept = aOpgenomenVermogen().withId(2L).withDatumTijd(dayToCleanup.atTime(0, 0, 1)).build();

        when(opgenomenVermogenRepository.getOpgenomenVermogen(dayToCleanup.atStartOfDay(), dayToCleanup.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(deleted, kept));

        opgenomenVermogenHousekeeping.start();

        assertThat(loggerEventCaptor.getAllValues())
            .haveExactly(1, new ContainsMessageAtLevel("OpgenomenVermogen(id=2, datumtijd=2016-01-01T00:00:01, watt=0, tariefIndicator=NORMAAL)", DEBUG))
            .haveExactly(1, new ContainsMessageAtLevel("OpgenomenVermogen(id=1, datumtijd=2016-01-01T00:00, watt=0, tariefIndicator=NORMAAL)", DEBUG));
    }
}
