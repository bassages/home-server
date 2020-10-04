package nl.homeserver.energie.standbypower;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import nl.homeserver.DatePeriod;
import nl.homeserver.DateTimePeriod;
import nl.homeserver.energie.opgenomenvermogen.NumberOfRecordsPerWatt;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenRepository;
import nl.homeserver.energie.verbruikkosten.ActuallyRegisteredVerbruikProvider;
import nl.homeserver.energie.verbruikkosten.VerbruikForVirtualUsageProvider;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzicht;
import nl.homeserver.energie.verbruikkosten.VerbruikKostenOverzichtService;

@Service
@AllArgsConstructor
class StandbyPowerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyPowerService.class);

    private static final String CACHE_NAME_STANDBY_POWER = "standbyPower";

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    private final ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    @Cacheable(CACHE_NAME_STANDBY_POWER)
    public Optional<StandbyPowerInPeriod> getStandbyPower(final YearMonth yearMonth) {
        LOGGER.info("getStandbyPower for yearMonth: {}", yearMonth);
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

        final List<NumberOfRecordsPerWatt> numberOfRecordsInRange = opgenomenVermogenRepository.numberOfRecordsInRange(
                period.getFromDateTime(), period.getToDateTime(), mostCommonWattInPeriod - 2, mostCommonWattInPeriod + 2);

        final long numberOfRecordsInStandbyPower = numberOfRecordsInRange.stream()
                                                                         .mapToLong(NumberOfRecordsPerWatt::getNumberOfRecords)
                                                                         .sum();
        final long totalNumberOfRecordsInQuarter = opgenomenVermogenRepository.countNumberOfRecordsInPeriod(period.getFromDateTime(), period.getToDateTime());

        final BigDecimal percentageInStandByPower = BigDecimal.valueOf(numberOfRecordsInStandbyPower)
                                                              .divide(BigDecimal.valueOf(totalNumberOfRecordsInQuarter), 2, RoundingMode.HALF_UP)
                                                              .multiply(BigDecimal.valueOf(100));

        final VerbruikKostenOverzicht actualVko = getActualVko(period);
        final VerbruikKostenOverzicht standByPowerVko = getStandbyPowerVko(mostCommonWattInPeriod, period);

        final StandbyPowerInPeriod standbyPowerInPeriod = new StandbyPowerInPeriod(
                datePeriod, mostCommonWattInPeriod, percentageInStandByPower, standByPowerVko, actualVko);

        return Optional.of(standbyPowerInPeriod);
    }

    private VerbruikKostenOverzicht getStandbyPowerVko(final int mostCommonWattInCurrentQuarter, final DateTimePeriod period) {
        return verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(new VerbruikForVirtualUsageProvider(mostCommonWattInCurrentQuarter), period);
    }

    private VerbruikKostenOverzicht getActualVko(final DateTimePeriod period) {
        return verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider, period);
    }
}
