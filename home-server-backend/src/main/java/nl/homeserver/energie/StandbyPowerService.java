package nl.homeserver.energie;

import static java.util.Comparator.naturalOrder;
import static nl.homeserver.DateTimePeriod.aPeriodWithToDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import nl.homeserver.DateTimePeriod;

@Service
public class StandbyPowerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandbyPowerService.class);

    public static final String CACHE_NAME_STANDBY_POWER = "standbyPower";

    private final OpgenomenVermogenRepository opgenomenVermogenRepository;
    private final VerbruikKostenOverzichtService verbruikKostenOverzichtService;
    private final ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider;

    public StandbyPowerService(final OpgenomenVermogenRepository opgenomenVermogenRepository,
                               final VerbruikKostenOverzichtService verbruikKostenOverzichtService,
                               final ActuallyRegisteredVerbruikProvider actuallyRegisteredVerbruikProvider) {
        this.opgenomenVermogenRepository = opgenomenVermogenRepository;
        this.verbruikKostenOverzichtService = verbruikKostenOverzichtService;
        this.actuallyRegisteredVerbruikProvider = actuallyRegisteredVerbruikProvider;
    }

    @Cacheable(CACHE_NAME_STANDBY_POWER)
    public Optional<StandbyPowerInPeriod> getStandbyPower(final YearMonth yearMonth) {
        LOGGER.info("getStandbyPower for yearMonth: {}", yearMonth);

        final LocalDate oldestAvailableDate = opgenomenVermogenRepository.getOldest().getDatumtijd().toLocalDate();
        final LocalDateTime from = Stream.of(oldestAvailableDate, yearMonth.atDay(1))
                                         .max(naturalOrder()).get().atStartOfDay();

        final LocalDate latestAvailableDate = opgenomenVermogenRepository.getMostRecent().getDatumtijd().toLocalDate();
        final LocalDateTime to = Stream.of(yearMonth.atEndOfMonth().plusDays(1), latestAvailableDate)
                                       .min(naturalOrder()).get().atStartOfDay();

        final Integer mostCommonWattInPeriod = opgenomenVermogenRepository.findMostCommonWattInPeriod(from, to);

        if (mostCommonWattInPeriod == null) {
            return Optional.empty();
        }

        final List<NumberOfRecordsPerWatt> numberOfRecordsInRange = opgenomenVermogenRepository.numberOfRecordsInRange(
                from, to, mostCommonWattInPeriod - 2, mostCommonWattInPeriod + 2);

        final long numberOfRecordsInStandbyPower = numberOfRecordsInRange.stream()
                                                                         .mapToLong(NumberOfRecordsPerWatt::getNumberOfRecords)
                                                                         .sum();
        final long totalNumberOfRecordsInQuarter = opgenomenVermogenRepository.countNumberOfRecordsInPeriod(from, to);

        final BigDecimal percentageInStandByPower = BigDecimal.valueOf(numberOfRecordsInStandbyPower)
                                                              .divide(BigDecimal.valueOf(totalNumberOfRecordsInQuarter), 2, RoundingMode.HALF_UP)
                                                              .multiply(BigDecimal.valueOf(100));

        final DateTimePeriod period = aPeriodWithToDateTime(from, to);

        final VerbruikKostenOverzicht actualVko = getActualVko(period);
        final VerbruikKostenOverzicht standByPowerVko = getStandbyPowerVko(mostCommonWattInPeriod, period);

        StandbyPowerInPeriod standbyPowerInPeriod = new StandbyPowerInPeriod(yearMonth, mostCommonWattInPeriod, percentageInStandByPower, standByPowerVko, actualVko);

        return Optional.of(standbyPowerInPeriod);
    }

    private VerbruikKostenOverzicht getStandbyPowerVko(final int mostCommonWattInCurrentQuarter, final DateTimePeriod period) {
        return verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(new VerbruikForVirtualUsageProvider(mostCommonWattInCurrentQuarter), period);
    }

    private VerbruikKostenOverzicht getActualVko(final DateTimePeriod period) {
        return verbruikKostenOverzichtService.getVerbruikEnKostenOverzicht(actuallyRegisteredVerbruikProvider, period);
    }
}
