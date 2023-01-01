package nl.homeserver.climate;

import lombok.RequiredArgsConstructor;
import nl.homeserver.DatePeriod;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class KlimaatService {

    private final KlimaatServiceHelper klimaatServiceHelper;
    private final KlimaatRepos klimaatRepository;
    private final Clock clock;

    void save(final Klimaat klimaat) {
        klimaatRepository.save(klimaat);
    }

    List<Klimaat> getInPeriod(final String climateSensorCode, final DatePeriod period) {
        final LocalDate today = LocalDate.now(clock);

        if (period.getEndDate().isBefore(today)) {
            return klimaatServiceHelper.getPotentiallyCachedAllInPeriod(climateSensorCode, period);
        } else {
            return klimaatServiceHelper.getNotCachedAllInPeriod(climateSensorCode, period);
        }
    }

    List<List<GemiddeldeKlimaatPerMaand>> getAveragePerMonthInYears(final String sensorCode,
                                                                    final SensorType sensorType,
                                                                    final int ... years) {
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
            return klimaatServiceHelper.getPotentiallyCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
        } else if (yearMonth.isAfter(YearMonth.now(clock))) {
            return new GemiddeldeKlimaatPerMaand(yearMonth.atDay(1), null);
        } else {
            return klimaatServiceHelper.getNotCachedAverageInMonthOfYear(sensorCode, sensorType, yearMonth);
        }
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

    void deleteByKlimaatSensor(final KlimaatSensor klimaatSensor) {
        this.klimaatRepository.deleteByKlimaatSensorCode(klimaatSensor.getCode());
    }
}
