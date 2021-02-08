package nl.homeserver.energie.verbruikkosten;

import nl.homeserver.DatePeriod;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandBuilder;
import nl.homeserver.energie.meterstand.MeterstandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import static java.time.Month.*;
import static java.util.stream.IntStream.range;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energie.meterstand.MeterstandBuilder.aMeterstand;
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
        final LocalDate from = LocalDate.of(2016, JANUARY, 2);
        final LocalDate to = LocalDate.of(2016, JANUARY, 4);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1))))
                                           .thenReturn(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtForDay2);

        final List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).satisfiesExactly(
                verbruikKostenDay1 -> {
                    assertThat(verbruikKostenDay1.getDag()).isEqualTo(from);
                    assertThat(verbruikKostenDay1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay1);
                },
                verbruikKostenDay2 -> {
                    assertThat(verbruikKostenDay2.getDag()).isEqualTo(from.plusDays(1));
                    assertThat(verbruikKostenDay2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay2);
                });
    }

    @Test
    void whenGetVerbruikPerUurOpDagThenVerbruikKostenOverzichtServiceIsCalledForAllHoursInDay() {
        final LocalDate day = LocalDate.of(2016, JANUARY, 2);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForHour1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(day.atStartOfDay(), day.atStartOfDay().plusHours(1))))
                                           .thenReturn(verbruikKostenOverzichtForHour1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForHour2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(day.atStartOfDay().plusHours(1), day.atStartOfDay().plusHours(2))))
                                           .thenReturn(verbruikKostenOverzichtForHour2);

        final List<VerbruikInUurOpDag> verbruikPerUurOpDag = verbruikService.getVerbruikPerUurOpDag(day);

        assertThat(verbruikPerUurOpDag).hasSize(24);

        final VerbruikInUurOpDag verbruikKostenForHour1 = verbruikPerUurOpDag.get(0);
        assertThat(verbruikKostenForHour1.getUur()).isZero();
        assertThat(verbruikKostenForHour1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour1);

        final VerbruikInUurOpDag verbruikKostenForHour2 = verbruikPerUurOpDag.get(1);
        assertThat(verbruikKostenForHour2.getUur()).isEqualTo(1);
        assertThat(verbruikKostenForHour2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour2);

        range(0, 24).forEach(hour ->
            verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(
                    actuallyRegisteredVerbruikProvider,
                    aPeriodWithToDateTime(day.atStartOfDay().plusHours(hour), day.atStartOfDay().plusHours(hour + 1)))
        );
    }

    @Test
    void whenGetVerbruikPerMaandInJaarThenVerbruikKostenOverzichtServiceIsCalledForAllMonthsInYear() {
        final int year = 2018;

        final VerbruikKostenOverzicht verbruikKostenOverzichtForJanuary = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(LocalDate.of(year, JANUARY, 1).atStartOfDay(),
                                      LocalDate.of(year, FEBRUARY, 1).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtForJanuary);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForFebruary = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(LocalDate.of(year, FEBRUARY, 1).atStartOfDay(),
                                      LocalDate.of(year, MARCH, 1).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtForFebruary);

        final List<VerbruikInMaandInJaar> verbruikPerMaandInJaar = verbruikService.getVerbruikPerMaandInJaar(Year.of(year));

        assertThat(verbruikPerMaandInJaar).hasSize(12);

        final VerbruikInMaandInJaar verbruikKostenForMonth1 = verbruikPerMaandInJaar.get(0);
        assertThat(verbruikKostenForMonth1.getMaand()).isEqualTo(1);
        assertThat(verbruikKostenForMonth1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForJanuary);

        final VerbruikInMaandInJaar verbruikKostenForHour2 = verbruikPerMaandInJaar.get(1);
        assertThat(verbruikKostenForHour2.getMaand()).isEqualTo(2);
        assertThat(verbruikKostenForHour2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForFebruary);

        range(1, 12).forEach(month -> {
                final LocalDateTime from = LocalDate.of(year, month, 1).atStartOfDay();
                final LocalDateTime to = LocalDate.of(year, month + 1, 1).atStartOfDay();
                verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(
                        actuallyRegisteredVerbruikProvider, aPeriodWithToDateTime(from, to));
            }
        );
    }

    @Test
    void givenMeterstandenExistsForTwoYearsWhenGetVerbruikPerJaarThenVerbruikKostenOverzichtServiceIsCalledForBothYears() {
        final Meterstand meterstandFor2018 = aMeterstand().withDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay()).build();
        when(meterstandService.getOldest()).thenReturn(meterstandFor2018);

        final Meterstand meterstandFor2019 = aMeterstand()
                .withDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay()).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandFor2019);

        final VerbruikKostenOverzicht verbruikKostenOverzichtFor2018 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay(),
                                      LocalDate.of(2019, JANUARY, 1).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtFor2018);

        final VerbruikKostenOverzicht verbruikKostenOverzichtFor2019 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay(),
                                      LocalDate.of(2020, JANUARY, 1).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtFor2019);

        final List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        assertThat(verbruikPerJaar).satisfiesExactly(
                verbruikInJaar2018 -> {
                    assertThat(verbruikInJaar2018.getJaar()).isEqualTo(2018);
                    assertThat(verbruikInJaar2018.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2018);
                },
                verbruikInJaar2019 -> {
                    assertThat(verbruikInJaar2019.getJaar()).isEqualTo(2019);
                    assertThat(verbruikInJaar2019.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2019);
                }
        );
    }

    @Test
    void givenNoMeterstandenExistsWhenGetVerbruikPerJaarThenResultIsEmpty() {
        when(meterstandService.getOldest()).thenReturn(null);

        final List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        assertThat(verbruikPerJaar).isEmpty();
    }

    @Test
    void whenGetGemiddeldeVerbruikEnKostenInPeriodeThenAveragesAreReturned() {
        final LocalDate from = LocalDate.of(2016, JANUARY, 2);
        final LocalDate to = LocalDate.of(2016, JANUARY, 4);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1))))
                                           .thenReturn(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                actuallyRegisteredVerbruikProvider,
                aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtForDay2);

        final VerbruikKostenOverzicht gemiddeldeVerbruikEnKostenInPeriode =
                verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(period);

        assertThat(gemiddeldeVerbruikEnKostenInPeriode).isNotNull();
    }
}
