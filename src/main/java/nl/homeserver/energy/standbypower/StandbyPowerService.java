package nl.homeserver.energy.standbypower;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energy.opgenomenvermogen.NumberOfRecordsPerWatt;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogenRepository;
import nl.homeserver.energy.verbruikkosten.ActuallyRegisteredVerbruikProvider;
import nl.homeserver.energy.verbruikkosten.VerbruikForVirtualUsageProvider;
import nl.homeserver.energy.verbruikkosten.VerbruikKostenOverzicht;
import nl.homeserver.energy.verbruikkosten.VerbruikKostenOverzichtService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.math.RoundingMode.HALF_UP;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_STANDBY_POWER;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

@Slf4j
@Service
@RequiredArgsConstructor
class StandbyPowerService {

    private static final BigDecimal NUMBER_OF_HOURS_IN_YEAR = BigDecimal.valueOf(365).multiply(BigDecimal.valueOf(24));

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    private final ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    @Cacheable(CACHE_NAME_STANDBY_POWER)
    public Optional<StandbyPowerInPeriod> getStandbyPower(final YearMonth yearMonth) {
        log.info("getStandbyPower for yearMonth: {}", yearMonth);
        final Optional<DatePeriod> period = getPeriod(yearMonth);
        return period.flatMap(this::forPeriod);
    }

    private Optional<DatePeriod> getPeriod(final YearMonth yearMonth) {
        final OpgenomenVermogen oldest = opgenomenVermogenRepository.getOldest();
        final OpgenomenVermogen mostRecent = opgenomenVermogenRepository.getMostRecent();

        if (oldest == null || mostRecent == null) {
             return Optional.empty();
        }

        final LocalDate oldestAvailableDate = oldest.getDatumtijd().toLocalDate();
        final LocalDate from = max(oldestAvailableDate, yearMonth.atDay(1));

        final LocalDate mostRecentAvailableDate = mostRecent.getDatumtijd().toLocalDate();
        final LocalDate to = min(yearMonth.atEndOfMonth().plusDays(1), mostRecentAvailableDate);

        return Optional.of(aPeriodWithToDate(from, to));
    }

    private Optional<StandbyPowerInPeriod> forPeriod(final DatePeriod datePeriod) {
        final DateTimePeriod period = datePeriod.toDateTimePeriod();

        final Integer mostCommonWattInPeriod = opgenomenVermogenRepository.findMostCommonWattInPeriod(period.getFromDateTime(), period.getToDateTime());
        if (mostCommonWattInPeriod == null) {
            return Optional.empty();
        }

        final int approxYearlyUsageKwh = NUMBER_OF_HOURS_IN_YEAR.multiply(
                        BigDecimal.valueOf(mostCommonWattInPeriod).setScale(3, HALF_UP).divide(BigDecimal.valueOf(1000), HALF_UP))
                .setScale(0, HALF_UP)
                .intValueExact();

        final List<NumberOfRecordsPerWatt> numberOfRecordsInRange = opgenomenVermogenRepository.numberOfRecordsInRange(
                period.getFromDateTime(), period.getToDateTime(), mostCommonWattInPeriod - 2, mostCommonWattInPeriod + 2);

        final long numberOfRecordsInStandbyPower = numberOfRecordsInRange.stream()
                                                                         .mapToLong(NumberOfRecordsPerWatt::getNumberOfRecords)
                                                                         .sum();
        final long totalNumberOfRecordsInQuarter = opgenomenVermogenRepository.countNumberOfRecordsInPeriod(period.getFromDateTime(), period.getToDateTime());

        final BigDecimal percentageInStandByPower = BigDecimal.valueOf(numberOfRecordsInStandbyPower)
                                                              .divide(BigDecimal.valueOf(totalNumberOfRecordsInQuarter), 2, HALF_UP)
                                                              .multiply(BigDecimal.valueOf(100));

        final VerbruikKostenOverzicht actualVko = getActualVko(period);
        final VerbruikKostenOverzicht standByPowerVko = getStandbyPowerVko(mostCommonWattInPeriod, period);

        final StandbyPowerInPeriod standbyPowerInPeriod = new StandbyPowerInPeriod(
                datePeriod, mostCommonWattInPeriod, approxYearlyUsageKwh, percentageInStandByPower, standByPowerVko, actualVko);

        return Optional.of(standbyPowerInPeriod);
    }

    private VerbruikKostenOverzicht getStandbyPowerVko(final int mostCommonWattInCurrentQuarter, final DateTimePeriod period) {
        return verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, new VerbruikForVirtualUsageProvider(mostCommonWattInCurrentQuarter));
    }

    private VerbruikKostenOverzicht getActualVko(final DateTimePeriod period) {
        return verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(period, actuallyRegisteredVerbruikProvider);
    }
}
