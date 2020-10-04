package nl.homeserver.energie.standbypower;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;
import static nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenBuilder.aOpgenomenVermogen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import nl.homeserver.energie.opgenomenvermogen.NumberOfRecordsPerWatt;
import nl.homeserver.energie.verbruikkosten.ActuallyRegisteredVerbruikProvider;
import nl.homeserver.energie.verbruikkosten.VerbruikForVirtualUsageProvider;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzicht;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzichtService;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenRepository;

@RunWith(MockitoJUnitRunner.class)
public class StandbyPowerServiceTest {

    @InjectMocks
    private StandbyPowerService standbyPowerService;

    @Mock
    private OpgenomenVermogenRepository opgenomenVermogenRepository;
    @Mock
    private VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    @Mock
    private ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    @Mock
    private VerbruikKostenOverzicht actualVko;
    @Mock
    private VerbruikKostenOverzicht standByPowerVko;

    @Test
    public void whenGetStandbyPowerThenReturned() {
        final YearMonth month = YearMonth.of(2018, 1);
        final LocalDateTime from = LocalDate.of(2018, JANUARY, 1).atStartOfDay();
        final LocalDateTime to = LocalDate.of(2018, FEBRUARY, 1).atStartOfDay();

        when(opgenomenVermogenRepository.getOldest()).thenReturn(aOpgenomenVermogen().withDatumTijd(from).build());
        when(opgenomenVermogenRepository.getMostRecent()).thenReturn(aOpgenomenVermogen().withDatumTijd(to).build());

        when(opgenomenVermogenRepository.findMostCommonWattInPeriod(from, to)).thenReturn(10);

        when(opgenomenVermogenRepository.countNumberOfRecordsInPeriod(from, to)).thenReturn(10L);

        final NumberOfRecordsPerWatt numberOfRecordsPerWatt1 = mock(NumberOfRecordsPerWatt.class);
        when(numberOfRecordsPerWatt1.getNumberOfRecords()).thenReturn(2L);

        final NumberOfRecordsPerWatt numberOfRecordsPerWatt2 = mock(NumberOfRecordsPerWatt.class);
        when(numberOfRecordsPerWatt2.getNumberOfRecords()).thenReturn(1L);

        when(opgenomenVermogenRepository.numberOfRecordsInRange(from, to, 8, 12))
                .thenReturn(List.of(numberOfRecordsPerWatt1, numberOfRecordsPerWatt2));

        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider, aPeriodWithToDateTime(from, to)))
                                           .thenReturn(actualVko);
        when(verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(any(VerbruikForVirtualUsageProvider.class), eq(aPeriodWithToDateTime(from, to))))
                                           .thenReturn(standByPowerVko);

        when(standByPowerVko.getTotaalStroomKosten()).thenReturn(BigDecimal.valueOf(50));
        when(actualVko.getTotaalStroomKosten()).thenReturn(BigDecimal.valueOf(200));

        final StandbyPowerInPeriod standbyPower = standbyPowerService.getStandbyPower(month).get();

        assertThat(standbyPower.getFromDate()).isEqualTo(from.toLocalDate());
        assertThat(standbyPower.getToDate()).isEqualTo(to.toLocalDate());
        assertThat(standbyPower.getPercentageOfTotalPeriod()).isEqualTo(new BigDecimal("30.00"));
        assertThat(standbyPower.getStandbyPower()).isEqualTo(10);
        assertThat(standbyPower.getTotalCostsOfPower()).isEqualTo(new BigDecimal("200"));
        assertThat(standbyPower.getCostsOfStandByPower()).isEqualTo(new BigDecimal("50"));
        assertThat(standbyPower.getPercentageOfTotalCost()).isEqualTo(new BigDecimal("25"));
    }
}