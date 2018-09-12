package nl.homeserver.energie;

import static java.math.BigDecimal.TEN;
import static java.time.Month.MAY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.DateTimePeriod;

@RunWith(MockitoJUnitRunner.class)
public class ActuallyRegisteredVerbruikProviderTest {

    @InjectMocks
    private ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    @Mock
    private VerbruikRepository verbruikRepository;

    @Test
    public void whenGetGasVerbruikThenRepositoryAskedForOneHourBefore() {
        final LocalDateTime from = LocalDate.of(2019, MAY, 1).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2019, MAY, 5).atStartOfDay();

        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        when(verbruikRepository.getGasVerbruikInPeriod(from.plusHours(1), to.plusHours(1))).thenReturn(TEN);

        BigDecimal gasVerbruik = actuallyRegisteredVerbruikProvider.getGasVerbruik(period);

        assertThat(gasVerbruik).isEqualTo(TEN);
    }

    @Test
    public void givenTariefPeriodeNormaalWhenGetStroomVerbruikThenDelegatedToRepository() {
        final LocalDateTime from = LocalDate.of(2019, MAY, 1).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2019, MAY, 5).atStartOfDay();

        when(verbruikRepository.getStroomVerbruikNormaalTariefInPeriod(from, to)).thenReturn(TEN);

        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        BigDecimal stroomVerbruik = actuallyRegisteredVerbruikProvider.getStroomVerbruik(period, StroomTariefIndicator.NORMAAL);

        assertThat(stroomVerbruik).isEqualTo(TEN);
    }

    @Test
    public void givenTariefPeriodeDalWhenGetStroomVerbruikThenDelegatedToRepository() {
        final LocalDateTime from = LocalDate.of(2019, MAY, 1).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2019, MAY, 5).atStartOfDay();

        when(verbruikRepository.getStroomVerbruikDalTariefInPeriod(from, to)).thenReturn(TEN);

        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        BigDecimal stroomVerbruik = actuallyRegisteredVerbruikProvider.getStroomVerbruik(period, StroomTariefIndicator.DAL);

        assertThat(stroomVerbruik).isEqualTo(TEN);
    }

    @Test
    public void givenUnsupportedTariefPeriodeWhenGetStroomVerbruikThenException() {
        final LocalDateTime from = LocalDate.of(2019, MAY, 1).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2019, MAY, 5).atStartOfDay();


        final DateTimePeriod period = aPeriodWithToDateTime(from, to);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> actuallyRegisteredVerbruikProvider.getStroomVerbruik(period, StroomTariefIndicator.ONBEKEND))
                .withMessage("Unexpected StroomTariefIndicator: [ONBEKEND]");

        verifyZeroInteractions(verbruikRepository);
    }
}