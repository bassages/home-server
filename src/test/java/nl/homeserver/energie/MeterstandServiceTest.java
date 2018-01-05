package nl.homeserver.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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

    @Captor
    private ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Before
    public void setup() {
        setField(meterstandService, "meterstandServiceProxyWithEnabledCaching", meterstandService);
    }

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

        Meterstand meterstand = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        when(meterstandRepository.findByDatumtijdBetween(any(), any())).thenReturn(singletonList(meterstand));

        meterstandService.dailyCleanup();

        verify(meterstandRepository, times(3)).findByDatumtijdBetween(any(), any());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void whenCleanupThenAllButFirstAndLastMeterstandPerHourAreDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        timeTravelTo(clock, dayToCleanup.plusDays(1).atStartOfDay());

        Meterstand meterstand1 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(12, 30, 0)).build();
        Meterstand meterstand4 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(12, 45, 0)).build();

        Meterstand meterstand5 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(13, 0, 0)).build();
        Meterstand meterstand6 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(13, 15, 0)).build();
        Meterstand meterstand7 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(13, 30, 0)).build();
        Meterstand meterstand8 = MeterstandBuilder.aMeterstand().withDateTime(dayToCleanup.atTime(13, 45, 0)).build();

        when(meterstandRepository.findByDatumtijdBetween(LocalDate.of(2016, JANUARY, 1).atStartOfDay(),
                                                         LocalDate.of(2016, JANUARY, 1).atStartOfDay().plusDays(1).minusNanos(1)))
                .thenReturn(asList(meterstand1, meterstand2, meterstand3, meterstand4, meterstand5, meterstand6, meterstand7, meterstand8));

        meterstandService.dailyCleanup();

        verify(meterstandRepository, times(2)).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues().get(0)).containsExactly(meterstand2, meterstand3);
        assertThat(deletedMeterstandCaptor.getAllValues().get(1)).containsExactly(meterstand6, meterstand7);
    }

    @Test
    public void whenSaveThenDelegatedToRepositoryAndEventSend() {
        Meterstand meterstand = MeterstandBuilder.aMeterstand().build();
        when(meterstandRepository.save(meterstand)).thenReturn(meterstand);

        Meterstand savedMeterstand = meterstandService.save(meterstand);

        assertThat(savedMeterstand).isSameAs(meterstand);

        verify(messagingTemplate).convertAndSend(MeterstandService.TOPIC, meterstand);
    }

    @Test
    public void givenMeterstandAlreadySavedWhenGetMostRecentThenMostRecentReturned() {
        when(meterstandRepository.save(any(Meterstand.class))).then(returnsFirstArg());

        Meterstand meterstand = MeterstandBuilder.aMeterstand().build();
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
        Meterstand oldestMeterstand = MeterstandBuilder.aMeterstand().build();

        when(meterstandRepository.getOldest()).thenReturn(oldestMeterstand);

        assertThat(meterstandService.getOldest()).isSameAs(oldestMeterstand);
    }

    @Test
    public void whenGetOldestOfTodayThenReturned() {
        LocalDate today = LocalDate.of(2017, JANUARY, 8);

        timeTravelTo(clock, today.atStartOfDay());

        Meterstand oldestMeterstandElectricity = MeterstandBuilder.aMeterstand().withStroomTarief1(new BigDecimal("100.000")).withStroomTarief2(new BigDecimal("200.000")).build();
        when(meterstandRepository.getOldestInPeriod(today.atStartOfDay(), today.atStartOfDay().plusDays(1).minusNanos(1)))
                .thenReturn(oldestMeterstandElectricity);

        Meterstand oldestMeterstandGas = MeterstandBuilder.aMeterstand().withGas(new BigDecimal("965.000")).build();
        when(meterstandRepository.getOldestInPeriod(today.atStartOfDay().plusHours(1), today.atStartOfDay().plusDays(1).plusHours(1).minusNanos(1)))
                .thenReturn(oldestMeterstandGas);

        Meterstand oldestOfToday = meterstandService.getOldestOfToday();
        assertThat(oldestOfToday.getStroomTarief1()).isEqualTo(oldestMeterstandElectricity.getStroomTarief1());
        assertThat(oldestOfToday.getStroomTarief2()).isEqualTo(oldestMeterstandElectricity.getStroomTarief2());
        assertThat(oldestOfToday.getGas()).isEqualTo(oldestMeterstandGas.getGas());
    }
}