package nl.homeserver.energie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.DatePeriod;

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
        final VerbruikKostenOverzicht verbruikKostenOverzicht = mock(VerbruikKostenOverzicht.class);
        when(verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(datePeriodCaptor.capture())).thenReturn(verbruikKostenOverzicht);

        final LocalDate from = LocalDate.of(2017, 4, 2);
        final LocalDate to = LocalDate.of(2017, 5, 19);

        final VerbruikKostenOverzicht gemiddeldeVerbruikPerDagInPeriode = energieController.getGemiddeldeVerbruikPerDag(from, to);

        assertThat(datePeriodCaptor.getValue().getFromDate()).isEqualTo(from);
        assertThat(datePeriodCaptor.getValue().getToDate()).isEqualTo(to);

        assertThat(gemiddeldeVerbruikPerDagInPeriode).isSameAs(verbruikKostenOverzicht);
    }

    @Test
    public void whenGetVerbruikPerDagThenDelegatedToVerbruikService() {
        final List<VerbruikKostenOpDag> verbruikKostenPerDag = List.of(mock(VerbruikKostenOpDag.class));
        when(verbruikService.getVerbruikPerDag(datePeriodCaptor.capture())).thenReturn(verbruikKostenPerDag);

        final LocalDate from = LocalDate.of(2017, 4, 2);
        final LocalDate to = LocalDate.of(2017, 5, 19);

        final List<VerbruikKostenOpDag> verbruikPerDag = energieController.getVerbruikPerDag(from, to);

        assertThat(datePeriodCaptor.getValue().getFromDate()).isEqualTo(from);
        assertThat(datePeriodCaptor.getValue().getToDate()).isEqualTo(to);

        Assertions.assertThat(verbruikPerDag).isEqualTo(verbruikKostenPerDag);
    }

    @Test
    public void whenGetVerbruikPerUurOpDagThenDelegatedToVerbruikService() {
        final List<VerbruikInUurOpDag> verbruikPerUurOpDag = List.of(mock(VerbruikInUurOpDag.class));
        final LocalDate day = LocalDate.of(2017, 4, 2);
        when(verbruikService.getVerbruikPerUurOpDag(day)).thenReturn(verbruikPerUurOpDag);

        assertThat(energieController.getVerbruikPerUurOpDag(day)).isEqualTo(verbruikPerUurOpDag);
    }

    @Test
    public void whenGetVerbruikPerJaarThenDelegatedToVerbruikService() {
        final List<VerbruikInJaar> verbruikPerJaar = List.of(mock(VerbruikInJaar.class));
        when(verbruikService.getVerbruikPerJaar()).thenReturn(verbruikPerJaar);

        assertThat(energieController.getVerbruikPerJaar()).isEqualTo(verbruikPerJaar);
    }

    @Test
    public void whenGetVerbruikPerMaandInJaarThenDelegatedToVerbruikService() {
        final int year = 2018;

        final List<VerbruikInMaandInJaar> verbruikPerMaandInJaar = List.of(mock(VerbruikInMaandInJaar.class));
        when(verbruikService.getVerbruikPerMaandInJaar(eq(Year.of(year)))).thenReturn(verbruikPerMaandInJaar);

        assertThat(energieController.getVerbruikPerMaandInJaar(year)).isEqualTo(verbruikPerMaandInJaar);
    }
}