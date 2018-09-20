package nl.homeserver.energie;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.stream.IntStream.range;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.DatePeriod;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikServiceTest {

    @InjectMocks
    private VerbruikService verbruikService;

    @Mock
    private MeterstandService meterstandService;
    @Mock
    private VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    @Mock
    private ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    @Test
    public void whenGetVerbruikPerDagForTwoDaysThenVerbruikKostenOverzichtServiceIsCalledForBothDays() {
        final LocalDate from = LocalDate.of(2016, JANUARY, 2);
        final LocalDate to = LocalDate.of(2016, JANUARY, 4);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1))))
                                           .thenReturn(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtForDay2);

        final List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).hasSize(2);

        final VerbruikKostenOpDag verbruikKostenDay1 = verbruikPerDag.get(0);
        assertThat(verbruikKostenDay1.getDag()).isEqualTo(from);
        assertThat(verbruikKostenDay1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOpDag verbruikKostenDay2 = verbruikPerDag.get(1);
        assertThat(verbruikKostenDay2.getDag()).isEqualTo(from.plusDays(1));
        assertThat(verbruikKostenDay2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay2);
    }

    @Test
    public void whenGetVerbruikPerUurOpDagThenVerbruikKostenOverzichtServiceIsCalledForAllHoursInDay() {
        final LocalDate day = LocalDate.of(2016, JANUARY, 2);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForHour1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(day.atStartOfDay(), day.atStartOfDay().plusHours(1))))
                .thenReturn(verbruikKostenOverzichtForHour1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForHour2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(day.atStartOfDay().plusHours(1), day.atStartOfDay().plusHours(2))))
                .thenReturn(verbruikKostenOverzichtForHour2);

        final List<VerbruikInUurOpDag> verbruikPerUurOpDag = verbruikService.getVerbruikPerUurOpDag(day);

        assertThat(verbruikPerUurOpDag).hasSize(24);

        final VerbruikInUurOpDag verbruikKostenForHour1 = verbruikPerUurOpDag.get(0);
        assertThat(verbruikKostenForHour1.getUur()).isEqualTo(0);
        assertThat(verbruikKostenForHour1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour1);

        final VerbruikInUurOpDag verbruikKostenForHour2 = verbruikPerUurOpDag.get(1);
        assertThat(verbruikKostenForHour2.getUur()).isEqualTo(1);
        assertThat(verbruikKostenForHour2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour2);

        range(0, 24).forEach(hour ->
            verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                                aPeriodWithToDateTime(day.atStartOfDay().plusHours(hour), day.atStartOfDay().plusHours(hour + 1)))
        );
    }

    @Test
    public void whenGetVerbruikPerMaandInJaarThenVerbruikKostenOverzichtServiceIsCalledForAllMonthsInYear() {
        final int year = 2018;

        final VerbruikKostenOverzicht verbruikKostenOverzichtForJanuary = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                    aPeriodWithToDateTime(LocalDate.of(year, JANUARY, 1).atStartOfDay(), LocalDate.of(year, FEBRUARY, 1).atStartOfDay())))
                .thenReturn(verbruikKostenOverzichtForJanuary);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForFebruary = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
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
                verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider, aPeriodWithToDateTime(from, to));
            }
        );
    }

    @Test
    public void givenMeterstandenExistsForTwoYearsWhenGetVerbruikPerJaarThenVerbruikKostenOverzichtServiceIsCalledForBothYears() {
        final Meterstand meterstandFor2018 = MeterstandBuilder.aMeterstand().withDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay()).build();
        when(meterstandService.getOldest()).thenReturn(meterstandFor2018);

        final Meterstand meterstandFor2019 = MeterstandBuilder.aMeterstand().withDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay()).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandFor2019);

        final VerbruikKostenOverzicht verbruikKostenOverzichtFor2018 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay(),
                                                                                               LocalDate.of(2019, JANUARY, 1).atStartOfDay())))
                                           .thenReturn(verbruikKostenOverzichtFor2018);

        final VerbruikKostenOverzicht verbruikKostenOverzichtFor2019 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay(),
                                                                                               LocalDate.of(2020, JANUARY, 1).atStartOfDay())))
                                            .thenReturn(verbruikKostenOverzichtFor2019);

        final List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        assertThat(verbruikPerJaar).hasSize(2);

        final VerbruikInJaar verbruikInJaar2018 = verbruikPerJaar.get(0);
        assertThat(verbruikInJaar2018.getJaar()).isEqualTo(2018);
        assertThat(verbruikInJaar2018.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2018);

        final VerbruikInJaar verbruikInJaar2019 = verbruikPerJaar.get(1);
        assertThat(verbruikInJaar2019.getJaar()).isEqualTo(2019);
        assertThat(verbruikInJaar2019.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2019);
    }

    @Test
    public void givenNoMeterstandenExistsWhenGetVerbruikPerJaarThenResultIsEmpty() {
        when(meterstandService.getOldest()).thenReturn(null);

        final List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        assertThat(verbruikPerJaar).isEmpty();
    }

    @Test
    public void whenGetGemiddeldeVerbruikEnKostenInPeriodeThenAveragesAreReturned() {
        final LocalDate from = LocalDate.of(2016, JANUARY, 2);
        final LocalDate to = LocalDate.of(2016, JANUARY, 4);
        final DatePeriod period = aPeriodWithToDate(from, to);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1))))
                .thenReturn(verbruikKostenOverzichtForDay1);

        final VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider,
                                                                         aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay())))
                .thenReturn(verbruikKostenOverzichtForDay2);

        final VerbruikKostenOverzicht gemiddeldeVerbruikEnKostenInPeriode = verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(period);
        assertThat(gemiddeldeVerbruikEnKostenInPeriode).isNotNull();
    }
}