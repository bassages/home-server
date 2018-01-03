package nl.wiegman.home.energie;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.MARCH;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithToDateTime;
import static nl.wiegman.home.energie.MeterstandBuilder.aMeterstand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.DatePeriod;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikServiceTest {

    @InjectMocks
    private VerbruikService verbruikService;

    @Mock
    private MeterstandService meterstandService;
    @Mock
    private VerbruikKostenOverzichtService verbruikKostenOverzichtService;

    @Test
    public void whenGetVerbruikPerDagForTwoDaysThenVerbruikKostenOverzichtServiceIsCalledForBothDays() {
        LocalDate from = LocalDate.of(2016, JANUARY, 2);
        LocalDate to = LocalDate.of(2016, JANUARY, 4);
        DatePeriod period = aPeriodWithToDate(from, to);

        VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(eq(aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1)))))
                .thenReturn(verbruikKostenOverzichtForDay1);

        VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(eq(aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay()))))
                .thenReturn(verbruikKostenOverzichtForDay2);

        List<VerbruikKostenOpDag> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        assertThat(verbruikPerDag).hasSize(2);

        VerbruikKostenOpDag verbruikKostenOpDag1 = verbruikPerDag.get(0);
        assertThat(verbruikKostenOpDag1.getDag()).isEqualTo(from);
        assertThat(verbruikKostenOpDag1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay1);

        VerbruikKostenOpDag verbruikKostenOpDag2 = verbruikPerDag.get(1);
        assertThat(verbruikKostenOpDag2.getDag()).isEqualTo(from.plusDays(1));
        assertThat(verbruikKostenOpDag2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForDay2);
    }

    @Test
    public void whenGetVerbruikPerUurOpDagThenVerbruikKostenOverzichtServiceIsCalledForAllHoursInDay() {
        LocalDate day = LocalDate.of(2016, JANUARY, 2);

        VerbruikKostenOverzicht verbruikKostenOverzichtForHour1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(aPeriodWithToDateTime(day.atStartOfDay(), day.atStartOfDay().plusHours(1))))
                .thenReturn(verbruikKostenOverzichtForHour1);

        VerbruikKostenOverzicht verbruikKostenOverzichtForHour2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(aPeriodWithToDateTime(day.atStartOfDay().plusHours(1), day.atStartOfDay().plusHours(2))))
                .thenReturn(verbruikKostenOverzichtForHour2);

        List<VerbruikInUurOpDag> verbruikPerUurOpDag = verbruikService.getVerbruikPerUurOpDag(day);

        assertThat(verbruikPerUurOpDag).hasSize(24);

        VerbruikInUurOpDag verbruikKostenForHour1 = verbruikPerUurOpDag.get(0);
        assertThat(verbruikKostenForHour1.getUur()).isEqualTo(0);
        assertThat(verbruikKostenForHour1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour1);

        VerbruikInUurOpDag verbruikKostenForHour2 = verbruikPerUurOpDag.get(1);
        assertThat(verbruikKostenForHour2.getUur()).isEqualTo(1);
        assertThat(verbruikKostenForHour2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForHour2);

        range(0, 24).forEach(hour ->
            verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(aPeriodWithToDateTime(day.atStartOfDay().plusHours(hour), day.atStartOfDay().plusHours(hour + 1)))
        );
    }

    @Test
    public void whenGetVerbruikPerMaandInJaarThenVerbruikKostenOverzichtServiceIsCalledForAllMonthsInYear() {
        int year = 2018;

        VerbruikKostenOverzicht verbruikKostenOverzichtForJanuary = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                    aPeriodWithToDateTime(LocalDate.of(year, JANUARY, 1).atStartOfDay(), LocalDate.of(year, FEBRUARY, 1).atStartOfDay())))
                .thenReturn(verbruikKostenOverzichtForJanuary);

        VerbruikKostenOverzicht verbruikKostenOverzichtForFebruary = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(year, FEBRUARY, 1).atStartOfDay(), LocalDate.of(year, MARCH, 1).atStartOfDay())))
                .thenReturn(verbruikKostenOverzichtForFebruary);

        List<VerbruikInMaandInJaar> verbruikPerMaandInJaar = verbruikService.getVerbruikPerMaandInJaar(Year.of(year));

        assertThat(verbruikPerMaandInJaar).hasSize(12);

        VerbruikInMaandInJaar verbruikKostenForMonth1 = verbruikPerMaandInJaar.get(0);
        assertThat(verbruikKostenForMonth1.getMaand()).isEqualTo(1);
        assertThat(verbruikKostenForMonth1.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForJanuary);

        VerbruikInMaandInJaar verbruikKostenForHour2 = verbruikPerMaandInJaar.get(1);
        assertThat(verbruikKostenForHour2.getMaand()).isEqualTo(2);
        assertThat(verbruikKostenForHour2.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtForFebruary);

        range(1, 12).forEach(month ->
            verify(verbruikKostenOverzichtService).getVerbruikEnKostenOverzicht(
                    aPeriodWithToDateTime(LocalDate.of(year, month, 1).atStartOfDay(), LocalDate.of(year, month + 1, 1).atStartOfDay()))
        );
    }

    @Test
    public void givenMeterstandenExistsForTwoYearsWhenGetVerbruikPerJaarThenVerbruikKostenOverzichtServiceIsCalledForBothYears() {
        Meterstand meterstandFor2018 = aMeterstand().withDateTime(LocalDate.of(2018, 1, 1).atStartOfDay()).build();
        when(meterstandService.getOldest()).thenReturn(meterstandFor2018);

        Meterstand meterstandFor2019 = aMeterstand().withDateTime(LocalDate.of(2019, 1, 1).atStartOfDay()).build();
        when(meterstandService.getMostRecent()).thenReturn(meterstandFor2019);

        VerbruikKostenOverzicht verbruikKostenOverzichtFor2018 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(2018, JANUARY, 1).atStartOfDay(), LocalDate.of(2019, JANUARY, 1).atStartOfDay())))
                .thenReturn(verbruikKostenOverzichtFor2018);

        VerbruikKostenOverzicht verbruikKostenOverzichtFor2019 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(
                aPeriodWithToDateTime(LocalDate.of(2019, JANUARY, 1).atStartOfDay(), LocalDate.of(2020, JANUARY, 1).atStartOfDay())))
                .thenReturn(verbruikKostenOverzichtFor2019);

        List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        assertThat(verbruikPerJaar).hasSize(2);

        VerbruikInJaar verbruikInJaar2018 = verbruikPerJaar.get(0);
        assertThat(verbruikInJaar2018.getJaar()).isEqualTo(2018);
        assertThat(verbruikInJaar2018.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2018);

        VerbruikInJaar verbruikInJaar2019 = verbruikPerJaar.get(1);
        assertThat(verbruikInJaar2019.getJaar()).isEqualTo(2019);
        assertThat(verbruikInJaar2019.getVerbruikKostenOverzicht()).isSameAs(verbruikKostenOverzichtFor2019);
    }

    @Test
    public void givenNoMeterstandenExistsWhenGetVerbruikPerJaarThenResultIsEmpty() {
        when(meterstandService.getOldest()).thenReturn(null);

        List<VerbruikInJaar> verbruikPerJaar = verbruikService.getVerbruikPerJaar();

        assertThat(verbruikPerJaar).isEmpty();
    }

    @Test
    public void whenGetGemiddeldeVerbruikEnKostenInPeriodeThenAveragesAreReturned() {
        LocalDate from = LocalDate.of(2016, JANUARY, 2);
        LocalDate to = LocalDate.of(2016, JANUARY, 4);
        DatePeriod period = aPeriodWithToDate(from, to);

        VerbruikKostenOverzicht verbruikKostenOverzichtForDay1 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(eq(aPeriodWithToDateTime(from.atStartOfDay(), from.atStartOfDay().plusDays(1)))))
                .thenReturn(verbruikKostenOverzichtForDay1);

        VerbruikKostenOverzicht verbruikKostenOverzichtForDay2 = new VerbruikKostenOverzicht();
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(eq(aPeriodWithToDateTime(from.plusDays(1).atStartOfDay(), from.plusDays(2).atStartOfDay()))))
                .thenReturn(verbruikKostenOverzichtForDay2);

        VerbruikKostenOverzicht verbruikKostenOverzichtAverages = mock(VerbruikKostenOverzicht.class);
        when(verbruikKostenOverzichtService.getAverages(asList(verbruikKostenOverzichtForDay1, verbruikKostenOverzichtForDay2))).thenReturn(verbruikKostenOverzichtAverages);

        assertThat(verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(period)).isSameAs(verbruikKostenOverzichtAverages);

        verify(verbruikKostenOverzichtService).getAverages(asList(verbruikKostenOverzichtForDay1, verbruikKostenOverzichtForDay2));
    }
}