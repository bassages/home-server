package nl.wiegman.home.energie;

import static java.time.Month.JANUARY;
import static java.util.Collections.emptyList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toList;
import static nl.wiegman.home.DateTimePeriod.aPeriodWithToDateTime;
import static nl.wiegman.home.DateTimeUtil.getDaysInPeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.DateTimePeriod;

@Service
public class VerbruikService {

    private final VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    private final MeterstandService meterstandService;

    @Autowired
    public VerbruikService(VerbruikKostenOverzichtService verbruikKostenOverzichtService, MeterstandService meterstandService) {
        this.verbruikKostenOverzichtService = verbruikKostenOverzichtService;
        this.meterstandService = meterstandService;
    }

    public List<VerbruikInUurOpDag> getVerbruikPerUurOpDag(LocalDate day) {
        return IntStream.range(0, 24)
                        .mapToObj(hourOfDay -> getVerbruikInUur(day, hourOfDay))
                        .collect(toList());
    }

    public List<VerbruikInMaandInJaar> getVerbruikPerMaandInJaar(Year year) {
        return allOf(Month.class).stream()
                                 .map(monthInYear -> getVerbruikInMaand(YearMonth.of(year.getValue(), monthInYear)))
                                 .collect(toList());
    }

    public List<VerbruikKostenOpDag> getVerbruikPerDag(DatePeriod period) {
        return getDaysInPeriod(period).stream()
                                      .map(this::getVerbruikOpDag)
                                      .collect(toList());
    }

    public List<VerbruikInJaar> getVerbruikPerJaar() {
        Meterstand oldest = meterstandService.getOldest();
        Meterstand mostRecent = meterstandService.getMostRecent();

        if (oldest == null) {
            return emptyList();
        }

        int from = oldest.getDateTime().getYear();
        int to = mostRecent.getDateTime().plusYears(1).getYear();

        return IntStream.range(from, to)
                        .mapToObj(year -> getVerbruikInJaar(Year.of(year)))
                        .collect(toList());
    }

    public VerbruikKostenOverzicht getGemiddeldeVerbruikEnKostenInPeriode(DatePeriod period) {
        List<VerbruikKostenOverzicht> verbruikKostenOverzichtPerDag = getVerbruikPerDag(period).stream().map(VerbruikKostenOpDag::getVerbruikKostenOverzicht).collect(toList());
        return verbruikKostenOverzichtService.getAverages(verbruikKostenOverzichtPerDag);
    }

    private VerbruikInJaar getVerbruikInJaar(Year year) {
        LocalDateTime from = LocalDate.of(year.getValue(), JANUARY, 1).atStartOfDay();
        LocalDateTime to = from.plusYears(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInJaar(year.getValue(), verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikInUurOpDag getVerbruikInUur(LocalDate day, int hour) {
        LocalDateTime from = day.atStartOfDay().plusHours(hour);
        LocalDateTime to = day.atStartOfDay().plusHours(hour + 1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInUurOpDag(hour, verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikInMaandInJaar getVerbruikInMaand(YearMonth yearMonth) {
        LocalDateTime from = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime to = yearMonth.atDay(1).atStartOfDay().plusMonths(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInMaandInJaar(yearMonth.getMonthValue(), verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period));
    }

    private VerbruikKostenOpDag getVerbruikOpDag(LocalDate day) {
        LocalDateTime from = day.atStartOfDay();
        LocalDateTime to = day.atStartOfDay().plusDays(1);
        DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikKostenOpDag(day, verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period));
    }
}