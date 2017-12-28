package nl.wiegman.home.energie;

import static java.time.Month.JANUARY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpoch;
import static nl.wiegman.home.energie.MeterstandBuilder.aMeterstand;
import static nl.wiegman.home.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import org.springframework.test.util.ReflectionTestUtils;

import nl.wiegman.home.cache.CacheService;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandServiceTest {

    @InjectMocks
    private MeterstandService meterstandService;

    @Mock
    private CacheService cacheService;
    @Mock
    private MeterstandRepository meterstandRepository;

    @Captor
    private ArgumentCaptor<List<Meterstand>> deletedMeterstandCaptor;

    @Before
    public void setup() {
        Clock clock = Clock.systemDefaultZone();
        createMeterstandService(clock);
    }

    private void createMeterstandService(Clock clock) {
        meterstandService = new MeterstandService(meterstandRepository, cacheService, clock);
        ReflectionTestUtils.setField(meterstandService, "meterstandServiceProxyWithEnabledCaching", meterstandService);
    }

    @Test
    public void shouldClearCacheOnCleanup() {
        meterstandService.dailyCleanup();
        verify(cacheService).clear(VerbruikService.CACHE_NAME_STROOM_VERBRUIK_IN_PERIODE);
        verify(cacheService).clear(VerbruikService.CACHE_NAME_GAS_VERBRUIK_IN_PERIODE);
    }

    @Test
    public void givenOnlyASingleMeterstandExistsWhenCleanupThenNoneDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        createMeterstandService(timeTravelTo(dayToCleanup.plusDays(1).atStartOfDay()));

        Meterstand meterstand = aMeterstand().withDatumTijd(dayToCleanup.atTime(12, 0, 0)).build();

        when(meterstandRepository.findByDatumtijdBetween(anyLong(), anyLong())).thenReturn(singletonList(meterstand));

        meterstandService.dailyCleanup();

        verify(meterstandRepository, times(3)).findByDatumtijdBetween(anyLong(), anyLong());
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    public void whenCleanupThenAllButFirstAndLastMeterstandPerHourAreDeleted() {
        LocalDate dayToCleanup = LocalDate.of(2016, JANUARY, 1);

        createMeterstandService(timeTravelTo(dayToCleanup.plusDays(1).atStartOfDay()));

        Meterstand meterstand1 = aMeterstand().withDatumTijd(dayToCleanup.atTime(12, 0, 0)).build();
        Meterstand meterstand2 = aMeterstand().withDatumTijd(dayToCleanup.atTime(12, 15, 0)).build();
        Meterstand meterstand3 = aMeterstand().withDatumTijd(dayToCleanup.atTime(12, 30, 0)).build();
        Meterstand meterstand4 = aMeterstand().withDatumTijd(dayToCleanup.atTime(12, 45, 0)).build();

        Meterstand meterstand5 = aMeterstand().withDatumTijd(dayToCleanup.atTime(13, 0, 0)).build();
        Meterstand meterstand6 = aMeterstand().withDatumTijd(dayToCleanup.atTime(13, 15, 0)).build();
        Meterstand meterstand7 = aMeterstand().withDatumTijd(dayToCleanup.atTime(13, 30, 0)).build();
        Meterstand meterstand8 = aMeterstand().withDatumTijd(dayToCleanup.atTime(13, 45, 0)).build();

        when(meterstandRepository.findByDatumtijdBetween(toMillisSinceEpoch(LocalDate.of(2016, JANUARY, 1).atStartOfDay()),
                toMillisSinceEpoch(LocalDate.of(2016, JANUARY, 1).atStartOfDay().plusDays(1).minusNanos(1))))
                .thenReturn(asList(meterstand1, meterstand2, meterstand3, meterstand4, meterstand5, meterstand6, meterstand7, meterstand8));

        meterstandService.dailyCleanup();

        verify(meterstandRepository, times(2)).deleteInBatch(deletedMeterstandCaptor.capture());

        assertThat(deletedMeterstandCaptor.getAllValues().get(0)).containsExactly(meterstand2, meterstand3);
        assertThat(deletedMeterstandCaptor.getAllValues().get(1)).containsExactly(meterstand6, meterstand7);
    }

    @Test
    public void whenSaveThenDelegatedToRepository() {
        Meterstand meterstand = aMeterstand().build();
        when(meterstandRepository.save(meterstand)).thenReturn(meterstand);

        Meterstand savedMeterstand = meterstandService.save(meterstand);

        assertThat(savedMeterstand).isSameAs(meterstand);
    }

    @Test
    public void whenGetMostRecentThenMostRecentReturned() {
        assertThat(meterstandService.getMostRecent()).isNull();

        Meterstand meterstand = aMeterstand().build();
        Meterstand savedMeterstand = meterstandService.save(meterstand);

        assertThat(meterstandService.getMostRecent()).isEqualTo(savedMeterstand);
    }

    @Test
    public void whenGetOldestDelegatedToRepository() {
        Meterstand oldestMeterstand = aMeterstand().build();

        when(meterstandRepository.getOudste()).thenReturn(oldestMeterstand);

        assertThat(meterstandService.getOldest()).isSameAs(oldestMeterstand);
    }

    @Test
    public void whenGetOldestOfTodayThenReturned() {
        LocalDate today = LocalDate.of(2017, JANUARY, 8);

        createMeterstandService(timeTravelTo(today.atStartOfDay()));

        Meterstand oldestMeterstandElectricity = aMeterstand().withStroomTarief1(new BigDecimal("100.000")).withStroomTarief2(new BigDecimal("200.000")).build();
        when(meterstandRepository.getOudsteInPeriode(toMillisSinceEpoch(today.atStartOfDay()), toMillisSinceEpoch(today.atStartOfDay().plusDays(1).minusNanos(1))))
                .thenReturn(oldestMeterstandElectricity);

        Meterstand oldestMeterstandGas = aMeterstand().withGas(new BigDecimal("965.000")).build();
        when(meterstandRepository.getOudsteInPeriode(toMillisSinceEpoch(today.atStartOfDay().plusHours(1)), toMillisSinceEpoch(today.atStartOfDay().plusDays(1).plusHours(1).minusNanos(1))))
                .thenReturn(oldestMeterstandGas);

        Meterstand oldestOfToday = meterstandService.getOldestOfToday();
        assertThat(oldestOfToday.getStroomTarief1()).isEqualTo(oldestMeterstandElectricity.getStroomTarief1());
        assertThat(oldestOfToday.getStroomTarief2()).isEqualTo(oldestMeterstandElectricity.getStroomTarief2());
        assertThat(oldestOfToday.getGas()).isEqualTo(oldestMeterstandGas.getGas());
    }
}