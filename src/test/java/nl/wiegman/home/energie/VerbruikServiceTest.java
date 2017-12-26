package nl.wiegman.home.energie;

import static java.time.Month.JANUARY;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpochAtStartOfDay;
import static nl.wiegman.home.energie.StroomTariefIndicator.DAL;
import static nl.wiegman.home.energie.StroomTariefIndicator.NORMAAL;
import static nl.wiegman.home.util.TimeMachine.timeTravelTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.DateTimePeriod;

@RunWith(MockitoJUnitRunner.class)
public class VerbruikServiceTest {

    private VerbruikService verbruikService;

    @Mock
    private MeterstandService meterstandService;
    @Mock
    private VerbruikServiceCached verbruikServiceCached;

    private void createVerbruikService(Clock clock) {
        verbruikService = new VerbruikService(meterstandService, verbruikServiceCached, clock);
    }

    @Test
    public void whenGetVerbruikPerDagForDateInFutureThenNoOtherServicesCalledAndUsageIsZero() {
        createVerbruikService(timeTravelTo(LocalDate.of(2016, JANUARY, 1).atStartOfDay()));

        LocalDate from = LocalDate.of(2016, JANUARY, 2);
        LocalDate to = LocalDate.of(2016, JANUARY, 3);
        DatePeriod period = aPeriodWithToDate(from, to);

        List<VerbruikOpDagDto> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        verifyZeroInteractions(verbruikServiceCached, meterstandService);

        assertThat(verbruikPerDag).hasSize(1);
        VerbruikOpDagDto verbruikOpDag = verbruikPerDag.get(0);
        assertThat(verbruikOpDag.getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from));
        assertThat(verbruikOpDag.getGasKosten()).isNull();
        assertThat(verbruikOpDag.getGasVerbruik()).isNull();
        assertThat(verbruikOpDag.getStroomKostenDal()).isNull();
        assertThat(verbruikOpDag.getStroomVerbruikDal()).isNull();
        assertThat(verbruikOpDag.getStroomKostenNormaal()).isNull();
        assertThat(verbruikOpDag.getStroomVerbruikNormaal()).isNull();
    }

    @Test
    public void whenGetVerbruikPerDagForPastTwoDaysThenUsagesAreRetrievedFromPotentiallyCachedService() {
        createVerbruikService(timeTravelTo(LocalDate.of(2016, JANUARY, 4).atStartOfDay()));

        LocalDate from = LocalDate.of(2016, JANUARY, 2);
        LocalDate to = LocalDate.of(2016, JANUARY, 4);
        DatePeriod period = aPeriodWithToDate(from, to);

        VerbruikKosten gasVerbruikKosten = createVerbruikKosten(new BigDecimal("3.021"), new BigDecimal("12.819024"));
        VerbruikKosten stroomDalVerbruikKosten = createVerbruikKosten(new BigDecimal("4.926"), new BigDecimal("0.762834"));
        VerbruikKosten stroomNormaalVerbruikKosten = createVerbruikKosten(new BigDecimal("0.000"), new BigDecimal("0.000000"));

        when(verbruikServiceCached.getPotentiallyCachedGasVerbruikInPeriode(any())).thenReturn(gasVerbruikKosten);
        when(verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(any(), eq(DAL))).thenReturn(stroomDalVerbruikKosten);
        when(verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(any(), eq(NORMAAL))).thenReturn(stroomNormaalVerbruikKosten);

        List<VerbruikOpDagDto> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        verifyZeroInteractions(meterstandService);

        assertThat(verbruikPerDag).hasSize(2);
        assertThat(verbruikPerDag.get(0).getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from));
        assertThat(verbruikPerDag.get(1).getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from.plusDays(1)));

        assertThat(verbruikPerDag).extracting(VerbruikDto::getGasKosten).containsOnly(gasVerbruikKosten.getKosten());
        assertThat(verbruikPerDag).extracting(VerbruikDto::getGasVerbruik).containsOnly(gasVerbruikKosten.getVerbruik());
        assertThat(verbruikPerDag).extracting(VerbruikDto::getStroomKostenDal).containsOnly(stroomDalVerbruikKosten.getKosten());
        assertThat(verbruikPerDag).extracting(VerbruikDto::getStroomVerbruikDal).containsOnly(stroomDalVerbruikKosten.getVerbruik());
        assertThat(verbruikPerDag).extracting(VerbruikDto::getStroomKostenNormaal).containsOnly(stroomNormaalVerbruikKosten.getKosten());
        assertThat(verbruikPerDag).extracting(VerbruikDto::getStroomVerbruikNormaal).containsOnly(stroomNormaalVerbruikKosten.getVerbruik());
    }

    @Test
    public void whenGetVerbruikPerDagForCurrentDayThenUsageAreRetrievedFromNonCachedService() {
        createVerbruikService(timeTravelTo(LocalDate.of(2016, JANUARY, 1).atTime(13, 10, 59)));

        LocalDate from = LocalDate.of(2016, JANUARY, 1);
        LocalDate to = LocalDate.of(2016, JANUARY, 2);
        DatePeriod period = aPeriodWithToDate(from, to);

        VerbruikKosten gasVerbruikKosten = createVerbruikKosten(new BigDecimal("3.021"), new BigDecimal("12.819024"));
        VerbruikKosten stroomDalVerbruikKosten = createVerbruikKosten(new BigDecimal("4.926"), new BigDecimal("0.762834"));
        VerbruikKosten stroomNormaalVerbruikKosten = createVerbruikKosten(new BigDecimal("0.000"), new BigDecimal("0.000000"));

        when(verbruikServiceCached.getGasVerbruikInPeriode(any())).thenReturn(gasVerbruikKosten);
        when(verbruikServiceCached.getStroomVerbruikInPeriode(any(), eq(DAL))).thenReturn(stroomDalVerbruikKosten);
        when(verbruikServiceCached.getStroomVerbruikInPeriode(any(), eq(NORMAAL))).thenReturn(stroomNormaalVerbruikKosten);

        List<VerbruikOpDagDto> verbruikPerDag = verbruikService.getVerbruikPerDag(period);

        verifyZeroInteractions(meterstandService);

        assertThat(verbruikPerDag).hasSize(1);
        VerbruikOpDagDto verbruikOpDag = verbruikPerDag.get(0);
        assertThat(verbruikOpDag.getDatumtijd()).isEqualTo(toMillisSinceEpochAtStartOfDay(from));
        assertThat(verbruikOpDag.getGasKosten()).isEqualTo(gasVerbruikKosten.getKosten());
        assertThat(verbruikOpDag.getGasVerbruik()).isEqualTo(gasVerbruikKosten.getVerbruik());
        assertThat(verbruikOpDag.getStroomKostenDal()).isEqualTo(stroomDalVerbruikKosten.getKosten());
        assertThat(verbruikOpDag.getStroomVerbruikDal()).isEqualTo(stroomDalVerbruikKosten.getVerbruik());
        assertThat(verbruikOpDag.getStroomKostenNormaal()).isEqualTo(stroomNormaalVerbruikKosten.getKosten());
        assertThat(verbruikOpDag.getStroomVerbruikNormaal()).isEqualTo(stroomNormaalVerbruikKosten.getVerbruik());
    }

    private VerbruikKosten createVerbruikKosten(BigDecimal verbruik, BigDecimal kosten) {
        VerbruikKosten verbruikKosten = new VerbruikKosten();
        verbruikKosten.setVerbruik(verbruik);
        verbruikKosten.setKosten(kosten);
        return verbruikKosten;
    }

}