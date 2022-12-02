package nl.homeserver.energy.verbruikkosten;

import nl.homeserver.DatePeriod;
import nl.homeserver.energy.meterreading.Meterstand;
import nl.homeserver.energy.meterreading.MeterstandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static java.time.Month.*;
import static java.util.stream.IntStream.range;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energy.meterreading.MeterstandBuilder.aMeterstand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerbruikServiceTest {

    @InjectMocks
    VerbruikService verbruikService;

    @Mock
    MeterstandService meterstandService;
    @Mock
    VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    @Mock
    ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    @Test
    void whenGetVerbruikPerDagForTwoDaysThenVerbruikKostenOverzichtServiceIsCalledForBothDays() {
        // given
        final LocalDate from = LocalDate.of(2016, JANUARY, 2);
        final LocalDate to = LocalDate.of(2016, JANUARY, 4);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1)), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay()), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForDay2);

        // when
        final List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        // then
        assertThat(verbruikPerDag).satisfiesExactly(
                verbruikKostenDay1 -> {
                    assertThat(verbruikKostenDay1.dag()).isEqualTo(from);
                    assertThat(verbruikKostenDay1.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay1);
                },
                verbruikKostenDay2 -> {
                    assertThat(verbruikKostenDay2.dag()).isEqualTo(from.plusDays(1));
                    assertThat(verbruikKostenDay2.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay2);
                });
    }

    @Test
    void whenGetVerbruikPerUurOpDagThenVerbruikKostenOverzichtServiceIsCalledForAllHoursInDay() {
        // given
        final LocalDate day = LocalDate.of(2016, JANUARY, 2);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForHour1 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(day.atStartOfDay(), day.atStartOfDay().plusHours(1)), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForHour1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForHour2 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(day.atStartOfDay().plusHours(1), day.atStartOfDay().plusHours(2)), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForHour2);

        // when
        final List<VerbruikInUurOpDag> verbruikPerUurOpDag = verbruikService.getVerbruikPerUurOpDag(day);

        // then
        assertThat(verbruikPerUurOpDag).hasSize(24);

        final VerbruikInUurOpDag verbruikKostenForHour1 = verbruikPerUurOpDag.get(0);
        assertThat(verbruikKostenForHour1.uur()).isZero();
        assertThat(verbruikKostenForHour1.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour1);

        final VerbruikInUurOpDag verbruikKostenForHour2 = verbruikPerUurOpDag.get(1);
        assertThat(verbruikKostenForHour2.uur()).isEqualTo(1);
        assertThat(verbruikKostenForHour2.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour2);

        range(0, 24).forEach(hour ->
            verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(
                    aPeriodWithToDateTime(day.atStartOfDay().plusHours(hour), day.atStartOfDay().plusHours(hour + 1)), actuallyRegisteredVerbruikProvider
            )
        );
    }

    @Test
    void whenGetVerbruikPerMaandInJaarThenVerbruikKostenOverzichtServiceIsCalledForAllMonthsInYear() {
        // given
        final int year = 2018;

        final VerbruikKostenOverzicht verbruikKostenOverzichtForJanuary = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(year, JANUARY, 1).atStartOfDay(),
                                      LocalDate.of(year, FEBRUARY, 1).atStartOfDay()), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForJanuary);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForFebruary = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(year, FEBRUARY, 1).atStartOfDay(),
                                      LocalDate.of(year, MARCH, 1).atStartOfDay()), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForFebruary);

        // when
        final List<VerbruikInMaandInJaar> verbruikPerMaandInJaar = verbruikService.getVerbruikPerMaandInJaar(Year.of(year));

        // then
        assertThat(verbruikPerMaandInJaar).hasSize(12);

        final VerbruikInMaandInJaar verbruikKostenForMonth1 = verbruikPerMaandInJaar.get(0);
        assertThat(verbruikKostenForMonth1.maand()).isEqualTo(1);
        assertThat(verbruikKostenForMonth1.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForJanuary);

        final VerbruikInMaandInJaar verbruikKostenForHour2 = verbruikPerMaandInJaar.get(1);
        assertThat(verbruikKostenForHour2.maand()).isEqualTo(2);
        assertThat(verbruikKostenForHour2.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForFebruary);

        range(1, 12).forEach(month -> {
                final LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
                final LocalDateTime to = LocalDate.of(year, month + 1, 1).atStartOfDay();
                verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(
                        aPeriodWithToDateTime(from, to), actuallyRegisteredVerbruikProvider);
            }
        );
    }

    @Test
    void givenMeterstandenExistsForTwoYearsWhenGetVerbruikPerJaarThenVerbruikKostenOverzichtServiceIsCalledForBothYears() {
        // given
        final Meterstand meterstandFor2018 = aMeterstand().withDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay()).build();
        when(meterstandService.getOldest()).thenReturn(Optional.of(meterstandFor2018));

        final Meterstand meterstandFor2019 = aMeterstand()
                .withDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay()).build();
        when(meterstandService.getMostRecent()).thenReturn(Optional.of(meterstandFor2019));

        final VerbruikKostenOverzicht verbruikKostenOverzichtFor2018 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay(),
                                      LocalDate.of(2019, JANUARY, 1).atStartOfDay()), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtFor2018);

        final VerbruikKostenOverzicht verbruikKostenOverzichtFor2019 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay(),
                                      LocalDate.of(2020, JANUARY, 1).atStartOfDay()), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtFor2019);

        // when
        final List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        // then
        assertThat(verbruikPerJaar).satisfiesExactly(
                verbruikInJaar2018 -> {
                    assertThat(verbruikInJaar2018.jaar()).isEqualTo(2018);
                    assertThat(verbruikInJaar2018.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2018);
                },
                verbruikInJaar2019 -> {
                    assertThat(verbruikInJaar2019.jaar()).isEqualTo(2019);
                    assertThat(verbruikInJaar2019.verbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2019);
                }
        );
    }

    @Test
    void givenNoMeterstandenExistsWhenGetVerbruikPerJaarThenResultIsEmpty() {
        // given
        when(meterstandService.getOldest()).thenReturn(Optional.empty());

        // when
        final List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        // then
        assertThat(verbruikPerJaar).isEmpty();
    }

    @Test
    void whenGetGemiddeldeVerbruikEnKostenInPeriodeThenAveragesAreReturned() {
        // given
        final LocalDate from = LocalDate.of(2016, JANUARY, 2);
        final LocalDate to = LocalDate.of(2016, JANUARY, 4);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1)), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = VerbruikKostenOverzicht.builder().build();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay()), actuallyRegisteredVerbruikProvider
        ))
                                           .thenReturn(verbruikKostenOverzichtForDay2);

        // when
        final VerbruikKostenOverzicht gemiddeldeVerbruikEnKostenInPeriode =
                verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(period);

        // then
        assertThat(gemiddeldeVerbruikEnKostenInPeriode).isNotNull();
    }
}
