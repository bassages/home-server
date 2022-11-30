package nl.homeserver.energie.verbruikkosten;

import nl.homeserver.DatePeriod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerbruikKostenControllerTest {

    @InjectMocks
    VerbruikKostenController verbruikKostenController;

    @Mock
    VerbruikService verbruikService;

    @Captor
    ArgumentCaptor<DatePeriod> datePeriodCaptor;

    @Test
    void whenGetGemiddeldeVerbruikPerDagThenDelegatedToVerbruikService() {
        final VerbruikKostenOverzicht verbruikKostenOverzicht = mock(VerbruikKostenOverzicht.class);
        when(verbruikService.getGemiddeldeVerbruikEnKostenInPeriode(datePeriodCaptor.capture())).thenReturn(verbruikKostenOverzicht);

        final LocalDate from = LocalDate.of(2017, 4, 2);
        final LocalDate to = LocalDate.of(2017, 5, 19);

        final VerbruikKostenOverzicht gemiddeldeVerbruikPerDagInPeriode = verbruikKostenController.getGemiddeldeVerbruikPerDag(from, to);

        assertThat(datePeriodCaptor.getValue().getFromDate()).isEqualTo(from);
        assertThat(datePeriodCaptor.getValue().getToDate()).isEqualTo(to);

        assertThat(gemiddeldeVerbruikPerDagInPeriode).isSameAs(verbruikKostenOverzicht);
    }

    @Test
    void whenGetVerbruikPerDagThenDelegatedToVerbruikService() {
        final List<VerbruikKostenOpDag> verbruikKostenPerDag = List.of(new VerbruikKostenOpDag(LocalDate.now(), mock(VerbruikKostenOverzicht.class)));
        when(verbruikService.getVerbruikPerDag(datePeriodCaptor.capture())).thenReturn(verbruikKostenPerDag);

        final LocalDate from = LocalDate.of(2017, 4, 2);
        final LocalDate to = LocalDate.of(2017, 5, 19);

        final List<VerbruikKostenOpDag> verbruikPerDag = verbruikKostenController.getVerbruikPerDag(from, to);

        assertThat(datePeriodCaptor.getValue().getFromDate()).isEqualTo(from);
        assertThat(datePeriodCaptor.getValue().getToDate()).isEqualTo(to);

        Assertions.assertThat(verbruikPerDag).isEqualTo(verbruikKostenPerDag);
    }

    @Test
    void whenGetVerbruikPerUurOpDagThenDelegatedToVerbruikService() {
        final List<VerbruikInUurOpDag> verbruikPerUurOpDag = List.of(new VerbruikInUurOpDag(2, mock(VerbruikKostenOverzicht.class)));
        final LocalDate day = LocalDate.of(2017, 4, 2);
        when(verbruikService.getVerbruikPerUurOpDag(day)).thenReturn(verbruikPerUurOpDag);

        assertThat(verbruikKostenController.getVerbruikPerUurOpDag(day)).isEqualTo(verbruikPerUurOpDag);
    }

    @Test
    void whenGetVerbruikPerJaarThenDelegatedToVerbruikService() {
        final List<VerbruikInJaar> verbruikPerJaar = List.of(new VerbruikInJaar(2022, mock(VerbruikKostenOverzicht.class)));
        when(verbruikService.getVerbruikPerJaar()).thenReturn(verbruikPerJaar);

        assertThat(verbruikKostenController.getVerbruikPerJaar()).isEqualTo(verbruikPerJaar);
    }

    @Test
    void whenGetVerbruikPerMaandInJaarThenDelegatedToVerbruikService() {
        final int year = 2018;

        final List<VerbruikInMaandInJaar> verbruikPerMaandInJaar = List.of(new VerbruikInMaandInJaar(5, mock(VerbruikKostenOverzicht.class)));
        when(verbruikService.getVerbruikPerMaandInJaar(Year.of(year))).thenReturn(verbruikPerMaandInJaar);

        assertThat(verbruikKostenController.getVerbruikPerMaandInJaar(year)).isEqualTo(verbruikPerMaandInJaar);
    }
}
