package nl.homeserver.klimaat;

import nl.homeserver.DatePeriod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.IntStream;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;

@Service
public class KlimaatService {
    private static final String CACHE_NAME_AVERAGE_KLIMAAT_IN_MONTH = "averageKlimaatInMonth";

    // Needed to make use of caching annotations
    @Autowired
    private KlimaatService klimaatServiceProxyWithEnabledCaching;

    private final KlimaatRepos klimaatRepository;
    private final Clock clock;

    KlimaatService(final KlimaatRepos klimaatRepository,
                   final Clock clock) {
        this.klimaatRepository = klimaatRepository;
        this.clock = clock;
    }

    void save(Klimaat klimaat) {
        klimaatRepository.save(klimaat);
    }

    List<Klimaat> getInPeriod(final String klimaatSensorCode,
                              final DatePeriod period) {
        final LocalDate today = LocalDate.now(clock);

        if (period.getEndDate().isBefore(today)) {
            return klimaatServiceProxyWithEnabledCaching.getPotentiallyCachedAllInPeriod(klimaatSensorCode, period);
        } else {
            return this.getNotCachedAllInPeriod(klimaatSensorCode, period);
        }
    }

    private BigDecimal getAverage(final String sensorCode, final SensorType sensorType, final DatePeriod period) {
        final LocalDateTime fromDateTime = period.getFromDate().atStartOfDay();
        final LocalDateTime toDateTime = period.getToDate().atStartOfDay();

        return switch (sensorType) {
            case LUCHTVOCHTIGHEID -> klimaatRepository.getAverageLuchtvochtigheid(sensorCode, fromDateTime, toDateTime);
            case TEMPERATUUR -> klimaatRepository.getAverageTemperatuur(sensorCode, fromDateTime, toDateTime);
        };
    }

    List<List<GemiddeldeKlimaatPerMaand>> getAveragePerMonthInYears(final String sensorCode,
                                                                    final SensorType sensorType,
                                                                    final int[] years) {
        return IntStream.of(years)
                        .mapToObj(Year::of)
                        .map(year -> getAveragePerMonthInYear(sensorCode, sensorType, year))
                        .toList();
    }

    private List<GemiddeldeKlimaatPerMaand> getAveragePerMonthInYear(final String sensorCode,
                                                                     final SensorType sensorType,
                                                                     final Year year) {
        return IntStream.rangeClosed(1, Month.values().length)
                        .mapToObj(month -> YearMonth.of(year.getValue(), month))
                        .map(yearMonth -> getAverageInMonthOfYear(sensorCode, sensorType, yearMonth))
                        .toList();
    }

    private GemiddeldeKlimaatPerMaand getAverageInMonthOfYear(final String sensorCode,
                                                              final SensorType sensorType,
                                                              final YearMonth yearMonth) {
        if (yearMonth.isBefore(YearMonth.now(clock))) {
            return klimaatServiceProxyWithEnabledCaching.getPotentiallyCachedAverageInMonthOfYear(sensorCode,
                                                                                                  sensorType,
                                                                                                  yearMonth);
        } else if (yearMonth.isAfter(YearMonth.now(clock))) {
            return new GemiddeldeKlimaatPerMaand(yearMonth.atDay(1), null);
        } else {
            return getNonCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
        }
    }

    @Cacheable(cacheNames = CACHE_NAME_AVERAGE_KLIMAAT_IN_MONTH)
    public GemiddeldeKlimaatPerMaand getPotentiallyCachedAverageInMonthOfYear(final String sensorCode,
                                                                              final SensorType sensorType,
                                                                              final YearMonth yearMonth) {
        return getNonCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
    }

    private GemiddeldeKlimaatPerMaand getNonCachedAverageInMonthOfYear(final String sensorCode,
                                                                       final SensorType sensorType,
                                                                       final YearMonth yearMonth) {
        final LocalDate from = yearMonth.atDay(1);
        final LocalDate to = from.plusMonths(1);
        final DatePeriod period = aPeriodWithToDate(from, to);
        return new GemiddeldeKlimaatPerMaand(from, getAverage(sensorCode, sensorType, period));
    }

    List<Klimaat> getHighest(final String sensorCode,
                             final SensorType sensorType,
                             final DatePeriod period,
                             final int limit) {
        return switch (sensorType) {
            case LUCHTVOCHTIGHEID -> getHighestHumidity(sensorCode, period, limit);
            case TEMPERATUUR -> getHighestTemperature(sensorCode, period, limit);
        };
    }

    List<Klimaat> getLowest(final String sensorCode,
                            final SensorType sensorType,
                            final DatePeriod period,
                            final int limit) {
        return switch (sensorType) {
            case LUCHTVOCHTIGHEID -> getLowestHumidity(sensorCode, period, limit);
            case TEMPERATUUR -> getLowestTemperature(sensorCode, period, limit);
        };
    }

    private List<Klimaat> getLowestTemperature(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakLowTemperatureDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestLowestTemperatureOnDay(sensorCode, day))
                                .toList();
    }

    private List<Klimaat> getLowestHumidity(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakLowHumidityDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestLowestHumidityOnDay(sensorCode, day))
                                .toList();
    }

    private List<Klimaat> getHighestTemperature(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakHighTemperatureDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestHighestTemperatureOnDay(sensorCode, day))
                                .toList();
    }

    private List<Klimaat> getHighestHumidity(final String sensorCode, final DatePeriod period, final int limit) {
        return klimaatRepository.getPeakHighHumidityDates(sensorCode, period.getFromDate(), period.getToDate(), limit)
                                .stream()
                                .map(java.sql.Date::toLocalDate)
                                .map(day -> klimaatRepository.earliestHighestHumidityOnDay(sensorCode, day))
                                .toList();
    }

    private List<Klimaat> getNotCachedAllInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getFromDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    @Cacheable(cacheNames = "klimaatInPeriod")
    public List<Klimaat> getPotentiallyCachedAllInPeriod(final String klimaatSensorCode, final DatePeriod period) {
        return klimaatRepository.findByKlimaatSensorCodeAndDatumtijdBetweenOrderByDatumtijd(klimaatSensorCode,
                                                                                            period.toDateTimePeriod().getFromDateTime(),
                                                                                            period.toDateTimePeriod().getEndDateTime());
    }

    void deleteByKlimaatSensor(final KlimaatSensor klimaatSensor) {
        this.klimaatRepository.deleteByKlimaatSensorCode(klimaatSensor.getCode());
    }
}
