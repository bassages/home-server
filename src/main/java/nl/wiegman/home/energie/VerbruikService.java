package nl.wiegman.home.energie;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithToDateTime;
import static nl.wiegman.home.DateTimeUtil.getDagenInPeriode;
import static nl.wiegman.home.DateTimeUtil.toMillisSinceEpochAtStartOfDay;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.DateTimePeriod;

@Service
public class VerbruikService {

    private final MeterstandService meterstandService;
    private final VerbruikServiceCached verbruikServiceCached;
    private final Clock clock;

    @Autowired
    public VerbruikService(MeterstandService meterstandService, VerbruikServiceCached verbruikServiceCached, Clock clock) {
        this.meterstandService = meterstandService;
        this.verbruikServiceCached = verbruikServiceCached;
        this.clock = clock;
    }

    public List<VerbruikInUurOpDagDto> getVerbruikPerUurOpDag(LocalDate day) {
        return IntStream.rangeClosed(0, 23)
                .mapToObj(hourOfDay -> getVerbruikInUur(day, hourOfDay))
                .collect(toList());
    }

    public List<VerbruikInMaandVanJaarDto> getVerbruikPerMaandInJaar(Year year) {
        return EnumSet.allOf(Month.class).stream()
                 .map(monthInYear -> getVerbruikInMaand(YearMonth.of(year.getValue(), monthInYear)))
                 .collect(toList());
    }

    public List<VerbruikOpDagDto> getVerbruikPerDag(DatePeriod period) {
        return getDagenInPeriode(period).stream()
                                        .map(this::getVerbruikOpDag)
                                        .collect(toList());
    }

    public List<VerbruikInJaarDto> getVerbruikPerJaar() {
        Meterstand oudste = meterstandService.getOudste();
        Meterstand nieuwste = meterstandService.getMeestRecente();

        if (oudste == null) {
            return emptyList();
        }

        int jaarVan = oudste.getDatumtijdAsLocalDateTime().getYear();
        int jaarTotEnMet = nieuwste.getDatumtijdAsLocalDateTime().getYear();

        return IntStream.rangeClosed(jaarVan, jaarTotEnMet)
                        .mapToObj(year -> getVerbruikInJaar(Year.of(year)))
                        .collect(toList());
    }

    private VerbruikInJaarDto getVerbruikInJaar(Year year) {
        VerbruikInJaarDto verbruikInJaarDto = new VerbruikInJaarDto();
        verbruikInJaarDto.setJaar(year.getValue());

        LocalDateTime from = LocalDate.of(year.getValue(), Month.JANUARY, 1).atStartOfDay();
        LocalDateTime to = from.plusYears(1);

        setVerbruik(aPeriodWithToDateTime(from, to), verbruikInJaarDto);

        return verbruikInJaarDto;
    }

    private VerbruikInUurOpDagDto getVerbruikInUur(LocalDate day, int hour) {
        VerbruikInUurOpDagDto verbruikInUurOpDag = new VerbruikInUurOpDagDto();
        verbruikInUurOpDag.setUur(hour);

        DateTimePeriod period = aPeriodWithToDateTime(day.atStartOfDay().plusHours(hour), day.atStartOfDay().plusHours(hour + 1));

        setVerbruik(period, verbruikInUurOpDag);

        return verbruikInUurOpDag;
    }

    private VerbruikInMaandVanJaarDto getVerbruikInMaand(YearMonth yearMonth) {
        VerbruikInMaandVanJaarDto verbruikInMaandVanJaar = new VerbruikInMaandVanJaarDto();
        verbruikInMaandVanJaar.setMaand(yearMonth.getMonthValue());

        LocalDateTime from = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime to = yearMonth.atDay(1).atStartOfDay().plusMonths(1);

        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        setVerbruik(period, verbruikInMaandVanJaar);

        return verbruikInMaandVanJaar;
    }

    private VerbruikOpDagDto getVerbruikOpDag(LocalDate day) {
        VerbruikOpDagDto verbruikOpDag = new VerbruikOpDagDto();
        verbruikOpDag.setDatumtijd(toMillisSinceEpochAtStartOfDay(day));

        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.atStartOfDay().plusDays(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);

        setVerbruik(period, verbruikOpDag);

        return verbruikOpDag;
    }

    private void setVerbruik(DateTimePeriod period, VerbruikDto verbruik) {
        setGasVerbruik(period, verbruik);
        setStroomVerbruikDalTarief(period, verbruik);
        setStroomVerbruikNormaalTarief(period, verbruik);
    }

    private void setGasVerbruik(DateTimePeriod period, VerbruikDto verbruik) {
        VerbruikKosten gasVerbruikKostenInPeriode = getGasVerbruikInPeriode(period);
        verbruik.setGasVerbruik(gasVerbruikKostenInPeriode.getVerbruik());
        verbruik.setGasKosten(gasVerbruikKostenInPeriode.getKosten());
    }

    private void setStroomVerbruikDalTarief(DateTimePeriod period, VerbruikDto verbruik) {
        VerbruikKosten stroomVerbruikKostenDalTariefInPeriode = getStroomVerbruikInPeriode(period, StroomTariefIndicator.DAL);
        verbruik.setStroomVerbruikDal(stroomVerbruikKostenDalTariefInPeriode.getVerbruik());
        verbruik.setStroomKostenDal(stroomVerbruikKostenDalTariefInPeriode.getKosten());
    }

    private void setStroomVerbruikNormaalTarief(DateTimePeriod period, VerbruikDto verbruik) {
        VerbruikKosten stroomVerbruikKostenNormaalTariefInPeriode = getStroomVerbruikInPeriode(period, StroomTariefIndicator.NORMAAL);
        verbruik.setStroomVerbruikNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getVerbruik());
        verbruik.setStroomKostenNormaal(stroomVerbruikKostenNormaalTariefInPeriode.getKosten());
    }

    private VerbruikKosten getGasVerbruikInPeriode(DateTimePeriod period) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (period.getStartDateTime().isAfter(now) || period.getStartDateTime().isEqual(now)) {
            return new VerbruikKosten();
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikServiceCached.getPotentiallyCachedGasVerbruikInPeriode(period);
        } else {
            return verbruikServiceCached.getGasVerbruikInPeriode(period);
        }
    }

    private VerbruikKosten getStroomVerbruikInPeriode(DateTimePeriod period, StroomTariefIndicator stroomTariefIndicator) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (period.getStartDateTime().isAfter(now) || period.getStartDateTime().isEqual(now)) {
            return new VerbruikKosten();
        } else if (period.getEndDateTime().isBefore(now)) {
            return verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(period, stroomTariefIndicator);
        } else {
            return verbruikServiceCached.getStroomVerbruikInPeriode(period, stroomTariefIndicator);
        }
    }
}