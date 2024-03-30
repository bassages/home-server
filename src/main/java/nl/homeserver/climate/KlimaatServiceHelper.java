package nl.homeserver.climate;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DatePeriod;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static nl.homeserver.CachingConfiguration.CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH;
import static nl.homeserver.CachingConfiguration.CACHE_NAME_CLIMATE_IN_PERIOD;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Service
@RequiredArgsConstructor
public class KlimaatServiceHelper {
    private final KlimaatRepos klimaatRepository;

    @Cacheable(cacheNames = CACHE_NAME_AVERAGE_CLIMATE_IN_MONTH)
    public GemiddeldeKlimaatPerMaand getPotentiallyCachedAverageInMonthOfYear(final String sensorCode,
                                                                              final SensorType sensorType,
                                                                              final YearMonth yearMonth) {
        return getNotCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
    }

    public GemiddeldeKlimaatPerMaand getNotCachedAverageInMonthOfYear(final String sensorCode,
                                                                      final SensorType sensorType,
                                                                      final YearMonth yearMonth) {
        final LocalDate from = yearMonth.atDay(1);
        final LocalDate to = from.plusMonths(1);
        final DatePeriod period = aPeriodWithToDate(from, to);
        return new GemiddeldeKlimaatPerMaand(from, getAverage(sensorCode, sensorType, period));
    }

    private BigDecimal getAverage(final String sensorCode, final SensorType sensorType, final DatePeriod period) {
        final LocalDateTime fromDateTime = period.getFromDate().atStartOfDay();
        final LocalDateTime toDateTime = period.getToDate().atStartOfDay();

        return switch (sensorType) {
            case LUCHTVOCHTIGHEID -> klimaatRepository.getAverageLuchtvochtigheid(sensorCode, fromDateTime, toDateTime);
            case TEMPERATUUR -> klimaatRepository.getAverageTemperatuur(sensorCode, fromDateTime, toDateTime);
        };
    }

    @Cacheable(cacheNames = CACHE_NAME_CLIMATE_IN_PERIOD)
    public List<Klimaat> getPotentiallyCachedAllInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        return getNotCachedAllInPeriod(klimaatSensorCode, period);
    }

    public List<Klimaat> getNotCachedAllInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                period.toDateTimePeriod().getFromDateTime(),
                period.toDateTimePeriod().getEndDateTime());
    }
}
