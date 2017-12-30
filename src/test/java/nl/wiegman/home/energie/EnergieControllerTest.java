package nl.wiegman.home.energie;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.DatePeriod;

@RunWith(MockitoJUnitRunner.class)
public class EnergieControllerTest {

    @InjectMocks
    private EnergieController energieController;

    @Mock
    private VerbruikService verbruikService;

    @Captor
    private ArgumentCaptor<DatePeriod> datePeriodCaptor;

    @Test
    public void whenGetGemiddeldeVerbruikPerDagThenDelegatedToVerbruikService() {
        VerbruikKostenOverzicht verbruikKostenOverzicht = mock(VerbruikKostenOverzicht.class);
        when(verbruikService.getGemiddeldeVerbruikPerDagInPeriode(datePeriodCaptor.capture())).thenReturn(verbruikKostenOverzicht);

        LocalDate from = LocalDate.of(2017, 4, 2);
        LocalDate to = LocalDate.of(2017, 5, 19);

        VerbruikKostenOverzicht gemiddeldeVerbruikPerDagInPeriode = energieController.getGemiddeldeVerbruikPerDag(from, to);

        assertThat(datePeriodCaptor.getValue().getFromDate()).isEqualTo(from);
        assertThat(datePeriodCaptor.getValue().getToDate()).isEqualTo(to);

        assertThat(gemiddeldeVerbruikPerDagInPeriode).isSameAs(verbruikKostenOverzicht);
    }

    @Test
    public void whenGetVerbruikPerDagThenDelegatedToVerbruikService() {
        List<VerbruikKostenOpDag> verbruikKostenPerDag = singletonList(mock(VerbruikKostenOpDag.class));
        when(verbruikService.getVerbruikPerDag(datePeriodCaptor.capture())).thenReturn(verbruikKostenPerDag);

        LocalDate from = LocalDate.of(2017, 4, 2);
        LocalDate to = LocalDate.of(2017, 5, 19);

        List<VerbruikKostenOpDag> verbruikPerDag = energieController.getVerbruikPerDag(from, to);

        assertThat(datePeriodCaptor.getValue().getFromDate()).isEqualTo(from);
        assertThat(datePeriodCaptor.getValue().getToDate()).isEqualTo(to);

        assertThat(verbruikPerDag).isEqualTo(verbruikKostenPerDag);
    }

    @Test
    public void whenGetVerbruikPerUurOpDagThenDelegatedToVerbruikService() {
        List<VerbruikInUurOpDag> verbruikPerUurOpDag = singletonList(mock(VerbruikInUurOpDag.class));
        LocalDate day = LocalDate.of(2017, 4, 2);
        when(verbruikService.getVerbruikPerUurOpDag(day)).thenReturn(verbruikPerUurOpDag);

        assertThat(energieController.getVerbruikPerUurOpDag(day)).isEqualTo(verbruikPerUurOpDag);
    }

    @Test
    public void whenGetVerbruikPerJaarThenDelegatedToVerbruikService() {
        List<VerbruikInJaar> verbruikPerJaar = singletonList(mock(VerbruikInJaar.class));
        when(verbruikService.getVerbruikPerJaar()).thenReturn(verbruikPerJaar);

        assertThat(energieController.getVerbruikPerJaar()).isEqualTo(verbruikPerJaar);
    }

    @Test
    public void whenGetVerbruikPerMaandInJaarThenDelegatedToVerbruikService() {
        int year = 2018;

        List<VerbruikInMaandInJaar> verbruikPerMaandInJaar = singletonList(mock(VerbruikInMaandInJaar.class));
        when(verbruikService.getVerbruikPerMaandInJaar(eq(Year.of(year)))).thenReturn(verbruikPerMaandInJaar);

        assertThat(energieController.getVerbruikPerMaandInJaar(year)).isEqualTo(verbruikPerMaandInJaar);
    }
}