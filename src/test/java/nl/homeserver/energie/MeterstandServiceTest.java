package nl.homeserver.energie;

import static ch.qos.logback.classic.Level.INFO;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static nl.homeserver.util.TimeMachine.useSystemDefaultClock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import nl.homeserver.LoggingRule;
import nl.homeserver.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandServiceTest {

    @InjectMocks
    private MeterstandService meterstandService;

    @Mock
    private CacheService cacheService;
    @Mock
    private MeterstandRepository meterstandRepository;
    @Mock
    private Clock clock;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private Appender appender;

    @Rule
    public LoggingRule loggingRule = new LoggingRule(getLogger(MeterstandService.class));

    @Captor
    private ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Test
    public void shouldClearCacheOnCleanup() {
        useSystemDefaultClock(clock);

        meterstandService.dailyCleanup();

        verify(cacheService).clear(VerbruikKostenOverzichtService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
        verify(cacheService).clear(VerbruikKostenOverzichtService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
    }

    @Test
    public void givenOnlyASingleMeterstandExistsWhenCleanupThenNoneDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        when(meterstandRepository.findByDateTimeBetween(any(), any())).thenReturn(singletonList(meterstand));

        meterstandService.dailyCleanup();

        verify(meterstandRepository, times(3)).findByDateTimeBetween(any(), any());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void whenCleanupThenAllButFirstAndLastMeterstandPerHourAreDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 30, 0)).build();
        Meterstand meterstand4 = aMeterstand().withDateTime(dayToCleanup.atTime(12, 45, 0)).build();

        Meterstand meterstand5 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 0, 0)).build();
        Meterstand meterstand6 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 15, 0)).build();
        Meterstand meterstand7 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 30, 0)).build();
        Meterstand meterstand8 = aMeterstand().withDateTime(dayToCleanup.atTime(13, 45, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(asList(meterstand1, meterstand2, meterstand3, meterstand4, meterstand5, meterstand6, meterstand7, meterstand8));

        meterstandService.dailyCleanup();

        verify(meterstandRepository, times(2)).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues().get(0)).containsExactly(meterstand2, meterstand3);
        assertThat(deletedMeterstandCaptor.getAllValues().get(1)).containsExactly(meterstand6, meterstand7);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCleanupThenKeptAndDeletedMeterstandenAreLogged() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(asList(meterstand1, meterstand2, meterstand3));

        loggingRule.setLevel(INFO);

        meterstandService.dailyCleanup();

        List<LoggingEvent> loggedEvents = loggingRule.getAllLoggedEvents();
        assertThat(loggedEvents).extracting(LoggingEvent::getLevel).containsOnly(INFO);
        assertThat(loggedEvents).areExactly(1, new LoggingEventMessageContaining("Keep first in hour 12: Meterstand[id=1"));
        assertThat(loggedEvents).areExactly(1, new LoggingEventMessageContaining("Keep last in hour 12: Meterstand[id=3"));
        assertThat(loggedEvents).areExactly(1, new LoggingEventMessageContaining("Delete: Meterstand[id=2"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenLogLevelIsNonewhenCleanupThenNothingLogged() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = aMeterstand().withId(1).withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withId(2).withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withId(3).withDateTime(dayToCleanup.atTime(12, 30, 0)).build();

        when(meterstandRepository.findByDateTimeBetween(dayToCleanup.atStartOfDay(),
                                                        dayToCleanup.atStartOfDay().plusDays(1).minusNanos(1)))
                .thenReturn(asList(meterstand1, meterstand2, meterstand3));

        loggingRule.setLevel(Level.OFF);

        meterstandService.dailyCleanup();

        assertThat(loggingRule.getAllLoggedEvents()).isEmpty();
    }

    @Test
    public void whenSaveThenDelegatedToRepositoryAndEventSend() {
        Meterstand meterstand = aMeterstand().build();
        when(meterstandRepository.save(meterstand)).thenReturn(meterstand);

        Meterstand savedMeterstand = meterstandService.save(meterstand);

        assertThat(savedMeterstand).isSameAs(meterstand);

        verify(messagingTemplate).convertAndSend(MeterstandService.TOPIC, meterstand);
    }

    @Test
    public void givenMeterstandAlreadySavedWhenGetMostRecentThenMostRecentReturned() {
        when(meterstandRepository.save(any(Meterstand.class))).then(returnsFirstArg());

        Meterstand meterstand = aMeterstand().build();
        Meterstand savedMeterstand = meterstandService.save(meterstand);

        assertThat(meterstandService.getMostRecent()).isEqualTo(savedMeterstand);
    }

    @Test
    public void givenNoMeterstandSavedYetWhenGetMostRecentThenMostRecentIsRetrievedFromRepository() {
        Meterstand meterstand = mock(Meterstand.class);
        when(meterstandRepository.getMostRecent()).thenReturn(meterstand);

        assertThat(meterstandService.getMostRecent()).isEqualTo(meterstand);
    }

    @Test
    public void whenGetOldestDelegatedToRepository() {
        Meterstand oldestMeterstand = aMeterstand().build();

        when(meterstandRepository.getOldest()).thenReturn(oldestMeterstand);

        assertThat(meterstandService.getOldest()).isSameAs(oldestMeterstand);
    }

    @Test
    public void whenGetOldestOfTodayThenReturned() {
        LocalDate today = LocalDate.of(2017, JANUARY, 8);

        timeTravelTo(clock, today.atStartOfDay());

        Meterstand oldestMeterstandElectricity = aMeterstand().withStroomTarief1(new BigDecimal("100.000"))
                                                              .withStroomTarief2(new BigDecimal("200.000"))
                                                              .build();

        when(meterstandRepository.getOldestInPeriod(today.atStartOfDay(), today.atStartOfDay().plusDays(1).minusNanos(1)))
                .thenReturn(oldestMeterstandElectricity);

        Meterstand oldestMeterstandGas = aMeterstand().withGas(new BigDecimal("965.000")).build();
        when(meterstandRepository.getOldestInPeriod(today.atStartOfDay().plusHours(1), today.atStartOfDay().plusDays(1).plusHours(1).minusNanos(1)))
                .thenReturn(oldestMeterstandGas);

        Meterstand oldestOfToday = meterstandService.getOldestOfToday();
        assertThat(oldestOfToday.getStroomTarief1()).isEqualTo(oldestMeterstandElectricity.getStroomTarief1());
        assertThat(oldestOfToday.getStroomTarief2()).isEqualTo(oldestMeterstandElectricity.getStroomTarief2());
        assertThat(oldestOfToday.getGas()).isEqualTo(oldestMeterstandGas.getGas());
    }

    @Test
    public void whenGetPerDagForTodayThenNonCachedMeterstandReturned() {
        setCachedMeterstandService(null);

        LocalDate today = LocalDate.of(2017, JANUARY, 13);

        timeTravelTo(clock, today.atStartOfDay());

        Meterstand mostRecentMeterstandOfToday = mock(Meterstand.class);
        when(meterstandRepository.getMostRecentInPeriod(today.atStartOfDay(), today.plusDays(1).atStartOfDay().minusNanos(1)))
                .thenReturn(mostRecentMeterstandOfToday);

        List<MeterstandOpDag> meterstandPerDag = meterstandService.getPerDag(aPeriodWithToDate(today, today.plusDays(1)));

        assertThat(meterstandPerDag).hasSize(1);
        assertThat(meterstandPerDag.get(0).getDag()).isEqualTo(today);
        assertThat(meterstandPerDag.get(0).getMeterstand()).isSameAs(mostRecentMeterstandOfToday);
    }

    @Test
    public void whenGetPerDagForYesterdayThenCachedMeterstandReturned() {
        MeterstandService cachedMeterstandService = mock(MeterstandService.class);
        setCachedMeterstandService(cachedMeterstandService);

        LocalDate today = LocalDate.of(2017, JANUARY, 13);
        timeTravelTo(clock, today.atStartOfDay());

        LocalDate yesterday = today.minusDays(1);

        Meterstand mostRecentMeterstandOfYesterday = mock(Meterstand.class);
        when(cachedMeterstandService.getPotentiallyCachedMeestRecenteMeterstandOpDag(yesterday)).thenReturn(mostRecentMeterstandOfYesterday);

        List<MeterstandOpDag> meterstandPerDag = meterstandService.getPerDag(aPeriodWithToDate(yesterday, yesterday.plusDays(1)));

        assertThat(meterstandPerDag).hasSize(1);
        assertThat(meterstandPerDag.get(0).getDag()).isEqualTo(yesterday);
        assertThat(meterstandPerDag.get(0).getMeterstand()).isSameAs(mostRecentMeterstandOfYesterday);
    }

    @Test
    public void whenGetPerDagForFutureThenNullReturned() {
        LocalDate today = LocalDate.of(2017, JANUARY, 13);
        timeTravelTo(clock, today.atStartOfDay());

        LocalDate tomorrow = today.plusDays(1);

        List<MeterstandOpDag> meterstandPerDag = meterstandService.getPerDag(aPeriodWithToDate(tomorrow, tomorrow.plusDays(1)));

        assertThat(meterstandPerDag).hasSize(1);
        assertThat(meterstandPerDag.get(0).getDag()).isEqualTo(tomorrow);
        assertThat(meterstandPerDag.get(0).getMeterstand()).isNull();
    }

    @Test
    public void whenGetPotentiallyCachedMeestRecenteMeterstandOpDagThenDelegatedToRepository() {
        LocalDate day = LocalDate.of(2016, MARCH, 12);

        Meterstand meterstand = mock(Meterstand.class);
        when(meterstandRepository.getMostRecentInPeriod(day.atStartOfDay(), day.plusDays(1).atStartOfDay().minusNanos(1))).thenReturn(meterstand);

        assertThat(meterstandService.getPotentiallyCachedMeestRecenteMeterstandOpDag(day)).isSameAs(meterstand);
    }

    private void setCachedMeterstandService(MeterstandService cachedMeterstandService) {
        setField(meterstandService, "meterstandServiceProxyWithEnabledCaching", cachedMeterstandService);
    }

    private class LoggingEventMessageContaining extends Condition<LoggingEvent> {
        private final String requiredContent;

        LoggingEventMessageContaining(String requiredContent) {
            super("Contains \"" + requiredContent + "\"");
            this.requiredContent = requiredContent;
        }

        @Override
        public boolean matches(LoggingEvent loggingEvent) {
            return loggingEvent.getFormattedMessage().contains(requiredContent);
        }
    }

}