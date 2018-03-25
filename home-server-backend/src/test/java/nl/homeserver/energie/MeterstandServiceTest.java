package nl.homeserver.energie;

import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energie.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class MeterstandServiceTest {

    @InjectMocks
    private MeterstandService meterstandService;

    @Mock
    private MeterstandRepository meterstandRepository;
    @Mock
    private Clock clock;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

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
    public void givenNoMeterstandExistsInNextHourWhenGetOldestOfTodayThenGasNotOverWritten() {
        LocalDate today = LocalDate.of(2017, JANUARY, 8);

        timeTravelTo(clock, today.atStartOfDay());

        Meterstand oldestMeterstandElectricity = aMeterstand().withStroomTarief1(new BigDecimal("100.000"))
                                                              .withStroomTarief2(new BigDecimal("200.000"))
                                                              .withGas(new BigDecimal("999.000"))
                                                              .build();

        when(meterstandRepository.getOldestInPeriod(today.atStartOfDay(), today.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(oldestMeterstandElectricity);

        when(meterstandRepository.getOldestInPeriod(today.atStartOfDay().plusHours(1), today.atStartOfDay().plusDays(1).plusHours(1).minusNanos(1)))
                                 .thenReturn(null);

        Meterstand oldestOfToday = meterstandService.getOldestOfToday();
        assertThat(oldestOfToday.getStroomTarief1()).isEqualTo(oldestMeterstandElectricity.getStroomTarief1());
        assertThat(oldestOfToday.getStroomTarief2()).isEqualTo(oldestMeterstandElectricity.getStroomTarief2());
        assertThat(oldestOfToday.getGas()).isEqualTo(oldestMeterstandElectricity.getGas());
    }

    @Test
    public void givenNoMeterstandExistsInPeriodWhenGetOldestOfTodayThenNullReturned() {
        LocalDate today = LocalDate.of(2017, JANUARY, 8);

        timeTravelTo(clock, today.atStartOfDay());

        when(meterstandRepository.getOldestInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                                 .thenReturn(null);

        Meterstand oldestOfToday = meterstandService.getOldestOfToday();
        assertThat(oldestOfToday).isNull();

        verify(meterstandRepository).getOldestInPeriod(today.atStartOfDay(), today.plusDays(1).atStartOfDay().minusNanos(1));
        verifyNoMoreInteractions(meterstandRepository);
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
}