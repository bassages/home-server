package nl.homeserver.energie.verbruikkosten;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
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
        return getPeriodForWhichDataIsAvailable()
                .stream()
                .flatMap(DatePeriod::streamYears)
                .map(year -> getVerbruikInJaar(Year.of(year)))
                .toList();
    }

    private Optional<DatePeriod> getPeriodForWhichDataIsAvailable() {
        final Optional<Meterstand> optionalOldest = meterstandService.getOldest();

        return optionalOldest.map(oldest -> {
            final Optional<Meterstand> optionalMostRecent = meterstandService.getMostRecent();
            final LocalDate from = oldest.getDateTime().toLocalDate();
            final LocalDate to = optionalMostRecent.map(
                    mostRecent -> mostRecent.getDateTime().toLocalDate()).orElse(from);
            return aPeriodWithToDate(from, to);
        });
    }

    VerbruikKostenOverzicht getGemiddeldeVerbruikEnKostenInPeriode(final DatePeriod period) {
        return getVerbruikPerDag(period).stream()
                                        .map(VerbruikKostenOpDag::getVerbruikKostenOverzicht)
                                        .collect(collectingAndThen(toList(), VerbruikKostenOverzichten::new))
                                        .averageToSingle();
    }

    private VerbruikInJaar getVerbruikInJaar(final Year year) {
        final DateTimePeriod period = DateTimePeriod.of(year);
        final VerbruikKostenOverzicht verbruikEnKostenOverzicht = verbruikKostenOverzichtService
                .getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider);
        return new VerbruikInJaar(year.getValue(), verbruikEnKostenOverzicht);
    }

    private VerbruikInUurOpDag getVerbruikInUur(final LocalDate day, final int hour) {
        final LocalDateTime from = day.atStartOfDay().plusHours(hour);
        final LocalDateTime to = from.plusHours(1);
        final DateTimePeriod period = aPeriodWithToDateTime(from, to);
        final VerbruikKostenOverzicht verbruikEnKostenOverzicht = verbruikKostenOverzichtService
                .getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider);
        return new VerbruikInUurOpDag(hour, verbruikEnKostenOverzicht);
    }

    private VerbruikInMaandInJaar getVerbruikInMaand(final YearMonth yearMonth) {
        final DateTimePeriod period = DateTimePeriod.of(yearMonth);
        final VerbruikKostenOverzicht verbruikEnKostenOverzicht = verbruikKostenOverzichtService
                .getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider);
        return new VerbruikInMaandInJaar(yearMonth.getMonthValue(), verbruikEnKostenOverzicht);
    }

    private VerbruikKostenOpDag getVerbruikOpDag(final LocalDate day) {
        final DateTimePeriod period = DateTimePeriod.of(day);
        final VerbruikKostenOverzicht verbruikEnKostenOverzicht = verbruikKostenOverzichtService
                .getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider);
        return new VerbruikKostenOpDag(day, verbruikEnKostenOverzicht);
    }
}
