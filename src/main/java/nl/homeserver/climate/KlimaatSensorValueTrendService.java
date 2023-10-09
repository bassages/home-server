package nl.homeserver.climate;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static nl.homeserver.DateTimeUtil.toMillisSinceEpoch;
import static nl.homeserver.climate.Trend.*;

@Service
class KlimaatSensorValueTrendService {

    private static final int MINIMUM_NUMBER_OF_ITEMS_TO_DETERMINE_TREND = 3;

    private static final Map<Integer, Trend> SIGNUM_OF_SLOPE_TO_TREND_MAPPING = new HashMap<>();

    KlimaatSensorValueTrendService() {
        SIGNUM_OF_SLOPE_TO_TREND_MAPPING.put(-1, DOWN);
        SIGNUM_OF_SLOPE_TO_TREND_MAPPING.put(0, STABLE);
        SIGNUM_OF_SLOPE_TO_TREND_MAPPING.put(1, UP);
    }

    Trend determineValueTrend(final List<Klimaat> klimaats, final Function<Klimaat, BigDecimal> sensorValueGetter) {
        final List<Klimaat> validklimaats = getValidKlimaats(klimaats, sensorValueGetter);

        if (validklimaats.size() < MINIMUM_NUMBER_OF_ITEMS_TO_DETERMINE_TREND) {
            return UNKNOWN;
        }

        final BigDecimal slopeOfSensorValue = calculateSlopeOfSensorValue(validklimaats, sensorValueGetter);
        return SIGNUM_OF_SLOPE_TO_TREND_MAPPING.get(slopeOfSensorValue.signum());
    }

    private List<Klimaat> getValidKlimaats(final List<Klimaat> klimaats, final Function<Klimaat, BigDecimal> sensorValueGetter) {
        return klimaats.stream()
                       .filter(klimaat -> nonNull(sensorValueGetter.apply(klimaat)))
                       .toList();
    }

    private BigDecimal calculateSlopeOfSensorValue(final List<Klimaat> klimaats, final Function<Klimaat, BigDecimal> sensorValueGetter) {
        final SimpleRegression simpleRegression = new SimpleRegression();
        klimaats.forEach(klimaat -> simpleRegression.addData(toMillisSinceEpoch(klimaat.getDatumtijd()), sensorValueGetter.apply(klimaat).doubleValue()));
        return BigDecimal.valueOf(simpleRegression.getSlope());
    }
}
