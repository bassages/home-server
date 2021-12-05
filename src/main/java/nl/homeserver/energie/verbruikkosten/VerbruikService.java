package nl.homeserver.energie.verbruikkosten;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;

@Service
@AllArgsConstructor
class VerbruikService {

    private final VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    private final MeterstandService meterstandService;
    private final ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    List<VerbruikInUurOpDag> getVerbruikPerUurOpDag(final LocalDate day) {
        return IntStream.range(0, 24)
                        .mapToObj(hourOfDay -> getVerbruikInUur(day, hourOfDay))
                        .toList();
    }

    List<VerbruikInMaandInJaar> getVerbruikPerMaandInJaar(final Year year) {
        return allOf(Month.class).stream()
                                 .map(monthInYear -> getVerbruikInMaand(YearMonth.of(year.getValue(), monthInYear)))
                                 .toList();
    }

    List<VerbruikKostenOpDag> getVerbruikPerDag(final DatePeriod period) {
        return period.getDays().stream()
                               .map(this::getVerbruikOpDag)
                               .toList();
    }

    List<VerbruikInJaar> getVerbruikPerJaar() {
        final Meterstand oldest = meterstandService.getOldest();
        final Meterstand mostRecent = meterstandService.getMostRecent();

        if (oldest == null) {
            return emptyList();
        }

        final int from = oldest.getDateTime().getYear();
        final int to = mostRecent.getDateTime().plusYears(1).getYear();

        return IntStream.range(from, to)
                        .mapToObj(year -> getVerbruikInJaar(Year.of(year)))
                        .toList();
    }

    VerbruikKostenOverzicht getGemiddeldeVerbruikEnKostenInPeriode(final DatePeriod period) {
        return getVerbruikPerDag(period).stream()
                                        .map(VerbruikKostenOpDag::getVerbruikKostenOverzicht)
                                        .collect(collectingAndThen(toList(), VerbruikKostenOverzichten::new))
                                        .averageToSingle();
    }

    private VerbruikInJaar getVerbruikInJaar(final Year year) {
        final DateTimePeriod period = DateTimePeriod.of(year);
        return new VerbruikInJaar(year.getValue(), verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider));
    }

    private VerbruikInUurOpDag getVerbruikInUur(final LocalDate day, final int hour) {
        final LocalDateTime from = day.atStartOfDay().plusHours(hour);
        final LocalDateTime to = from.plusHours(1);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);
        return new VerbruikInUurOpDag(hour, verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider));
    }

    private VerbruikInMaandInJaar getVerbruikInMaand(final YearMonth yearMonth) {
        final DateTimePeriod period = DateTimePeriod.of(yearMonth);
        return new VerbruikInMaandInJaar(yearMonth.getMonthValue(), verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider));
    }

    private VerbruikKostenOpDag getVerbruikOpDag(final LocalDate day) {
        final DateTimePeriod period = DateTimePeriod.of(day);
        return new VerbruikKostenOpDag(day, verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider));
    }
}
