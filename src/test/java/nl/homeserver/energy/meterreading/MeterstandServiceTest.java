package nl.homeserver.energy.meterreading;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.energy.meterreading.MeterstandBuilder.aMeterstand;
import static nl.homeserver.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterstandServiceTest {

    @InjectMocks
    MeterstandService meterstandService;

    @Mock
    MostResentMeterstandOpDagService mostResentMeterstandOpDagService;
    @Mock
    MeterstandRepository meterstandRepository;
    @Mock
    Clock clock;
    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Test
    void whenSaveThenDelegatedToRepositoryAndEventSend() {
        // given
        final Meterstand meterstand = aMeterstand().build();
        when(meterstandRepository.save(meterstand)).thenReturn(meterstand);

        // when
        final Meterstand savedMeterstand = meterstandService.save(meterstand);

        // then
        assertThat(savedMeterstand).isSameAs(meterstand);
        verify(messagingTemplate).convertAndSend(MeterstandService.TOPIC, meterstand);
    }

    @Test
    void givenMeterstandAlreadySavedWhenGetMostRecentThenMostRecentReturned() {
        // given
        when(meterstandRepository.save(any(Meterstand.class))).then(returnsFirstArg());
        final Meterstand meterstand = aMeterstand().build();
        final Meterstand savedMeterstand = meterstandService.save(meterstand);

        // when
        final Optional<Meterstand> mostRecent = meterstandService.getMostRecent();

        // then
        assertThat(mostRecent).contains(savedMeterstand);
    }

    @Test
    void givenNoMeterstandSavedYetWhenGetMostRecentThenMostRecentIsRetrievedFromRepository() {
        // given
        final Meterstand meterstand = mock(Meterstand.class);
        when(meterstandRepository.getMostRecent()).thenReturn(Optional.of(meterstand));

        // when
        final Optional<Meterstand> mostRecent = meterstandService.getMostRecent();

        // then
        assertThat(mostRecent).contains(meterstand);
    }

    @Test
    void whenGetOldestDelegatedToRepository() {
        // given
        final Meterstand meterstand = aMeterstand().build();
        when(meterstandRepository.getOldest()).thenReturn(Optional.of(meterstand));

        // when
        final Optional<Meterstand> oldest = meterstandService.getOldest();

        // then
        assertThat(oldest).contains(meterstand);
    }

    @Test
    void whenGetOldestOfTodayThenReturned() {
        // given
        final LocalDate today = LocalDate.of(2017, JANUARY, 8);
        timeTravelTo(clock, today.atStartOfDay());

        final Meterstand oldestMeterstandElectricity = aMeterstand().withStroomTarief1(new BigDecimal("100.000"))
                                                              .withStroomTarief2(new BigDecimal("200.000"))
                                                              .build();

        when(meterstandRepository.findOldestInPeriod(today.atStartOfDay(), today.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(Optional.of(oldestMeterstandElectricity));

        final Meterstand oldestMeterstandGas = aMeterstand().withGas(new BigDecimal("965.000")).build();
        when(meterstandRepository.findOldestInPeriod(today.atStartOfDay().plusHours(1), today.atStartOfDay().plusDays(1).plusHours(1).minusNanos(1)))
                                 .thenReturn(Optional.of(oldestMeterstandGas));

        // when
        final Optional<Meterstand> optionalOldestOfToday = meterstandService.findOldestOfToday();

        // then
        assertThat(optionalOldestOfToday).hasValueSatisfying(oldestOfToday -> {
            assertThat(oldestOfToday.getStroomTarief1()).isEqualTo(oldestMeterstandElectricity.getStroomTarief1());
            assertThat(oldestOfToday.getStroomTarief2()).isEqualTo(oldestMeterstandElectricity.getStroomTarief2());
            assertThat(oldestOfToday.getGas()).isEqualTo(oldestMeterstandGas.getGas());
        });
    }

    @Test
    void givenNoMeterstandExistsInNextHourWhenGetOldestOfTodayThenGasNotOverWritten() {
        // given
        final LocalDate today = LocalDate.of(2017, JANUARY, 8);

        timeTravelTo(clock, today.atStartOfDay());

        final Meterstand oldestMeterstandElectricity = aMeterstand().withStroomTarief1(new BigDecimal("100.000"))
                                                              .withStroomTarief2(new BigDecimal("200.000"))
                                                              .withGas(new BigDecimal("999.000"))
                                                              .build();

        when(meterstandRepository.findOldestInPeriod(today.atStartOfDay(), today.atStartOfDay().plusDays(1).minusNanos(1)))
                                 .thenReturn(Optional.of(oldestMeterstandElectricity));
        when(meterstandRepository.findOldestInPeriod(today.atStartOfDay().plusHours(1), today.atStartOfDay().plusDays(1).plusHours(1).minusNanos(1)))
                                 .thenReturn(Optional.empty());

        // when
        final Optional<Meterstand> optionalOldestOfToday = meterstandService.findOldestOfToday();

        // then
        assertThat(optionalOldestOfToday).hasValueSatisfying(oldestOfToday -> {
            assertThat(oldestOfToday.getStroomTarief1()).isEqualTo(oldestMeterstandElectricity.getStroomTarief1());
            assertThat(oldestOfToday.getStroomTarief2()).isEqualTo(oldestMeterstandElectricity.getStroomTarief2());
            assertThat(oldestOfToday.getGas()).isEqualTo(oldestMeterstandElectricity.getGas());
        });
    }

    @Test
    void givenNoMeterstandExistsInPeriodWhenGetOldestOfTodayThenEmptyOptionalReturned() {
        // given
        final LocalDate today = LocalDate.of(2017, JANUARY, 8);
        timeTravelTo(clock, today.atStartOfDay());
        when(meterstandRepository.findOldestInPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                                 .thenReturn(Optional.empty());

        // when
        final Optional<Meterstand> optionalOldestOfToday = meterstandService.findOldestOfToday();

        // then
        assertThat(optionalOldestOfToday).isEmpty();
        verify(meterstandRepository).findOldestInPeriod(today.atStartOfDay(), today.plusDays(1).atStartOfDay().minusNanos(1));
        verifyNoMoreInteractions(meterstandRepository);
    }

    @Test
    void whenGetPerDagForTodayThenNotCachedMeterstandReturned() {
        // given
        final LocalDate today = LocalDate.of(2017, JANUARY, 13);

        timeTravelTo(clock, today.atStartOfDay());

        final Meterstand mostRecentMeterstandOfToday = mock(Meterstand.class);
        when(mostResentMeterstandOpDagService.getNotCachedMeestRecenteMeterstandOpDag(today))
                .thenReturn(Optional.of(mostRecentMeterstandOfToday));

        // when
        final List<MeterstandOpDag> meterstandPerDag = meterstandService.getPerDag(aPeriodWithToDate(today, today.plusDays(1)));

        // then
        assertThat(meterstandPerDag).satisfiesExactly(meterstandOpDag -> {
            assertThat(meterstandOpDag.dag()).isEqualTo(today);
            assertThat(meterstandOpDag.meterstand()).isSameAs(mostRecentMeterstandOfToday);
        });
    }

    @Test
    void whenGetPerDagForYesterdayThenCachedMeterstandReturned() {
        // given
        final LocalDate today = LocalDate.of(2017, JANUARY, 13);
        timeTravelTo(clock, today.atStartOfDay());

        final LocalDate yesterday = today.minusDays(1);

        final Meterstand mostRecentMeterstandOfYesterday = mock(Meterstand.class);
        when(mostResentMeterstandOpDagService.getPotentiallyCachedMeestRecenteMeterstandOpDag(yesterday))
                .thenReturn(Optional.of(mostRecentMeterstandOfYesterday));

        // when
        final List<MeterstandOpDag> meterstandPerDag = meterstandService.getPerDag(aPeriodWithToDate(yesterday, yesterday.plusDays(1)));

        // then
        assertThat(meterstandPerDag).satisfiesExactly(meterstandOpDag -> {
            assertThat(meterstandOpDag.dag()).isEqualTo(yesterday);
            assertThat(meterstandOpDag.meterstand()).isSameAs(mostRecentMeterstandOfYesterday);
        });
    }

    @Test
    void whenGetPerDagForFutureThenNullReturned() {
        // given
        final LocalDate today = LocalDate.of(2017, JANUARY, 13);
        timeTravelTo(clock, today.atStartOfDay());

        final LocalDate tomorrow = today.plusDays(1);

        // when
        final List<MeterstandOpDag> meterstandPerDag = meterstandService.getPerDag(aPeriodWithToDate(tomorrow, tomorrow.plusDays(1)));

        // then
        assertThat(meterstandPerDag).satisfiesExactly(meterstandOpDag -> {
            assertThat(meterstandOpDag.dag()).isEqualTo(tomorrow);
            assertThat(meterstandOpDag.meterstand()).isNull();
        });
    }
}
