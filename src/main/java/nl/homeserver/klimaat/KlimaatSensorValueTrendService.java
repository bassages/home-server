package nl.homeserver.klimaat;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import nl.homeserver.DateTimeUtil;
import nl.homeserver.Trend;

@Service
public class KlimaatSensorValueTrendService {

    private static final int MINIMUM_NUMBER_OF_ITEMS_TO_DETERMINE_TREND = 3;

    private static final Map<Integer, Trend> SIGNUM_OF_SLOPE_TO_TREND_MAPPING = new HashMap<Integer, Trend>() {
        {
            put(-1, Trend.DOWN);
            put(0, Trend.STABLE);
            put(1, Trend.UP);
        }
    };

    public Trend determineValueTrend(List<Klimaat> klimaats, Function<Klimaat, BigDecimal> sensorValueGetter) {
        List<Klimaat> validklimaats = klimaats.stream()
                .filter(klimaat -> nonNull(sensorValueGetter.apply(klimaat)))
                .collect(toList());

        if (isNotEmpty(validklimaats) && validklimaats.size() >= MINIMUM_NUMBER_OF_ITEMS_TO_DETERMINE_TREND) {
            BigDecimal slopeOfSensorValue = calculateSlopeOfSensorValue(validklimaats, sensorValueGetter);
            return SIGNUM_OF_SLOPE_TO_TREND_MAPPING.get(slopeOfSensorValue.signum());
        }
        return null;
    }

    private BigDecimal calculateSlopeOfSensorValue(List<Klimaat> klimaats, Function<Klimaat, BigDecimal> sensorValueGetter) {
        SimpleRegression simpleRegression = new SimpleRegression();
        klimaats.forEach(klimaat -> simpleRegression.addData(DateTimeUtil.toMillisSinceEpoch(klimaat.getDatumtijd()), sensorValueGetter.apply(klimaat).doubleValue()));
        return new BigDecimal(simpleRegression.getSlope());
    }
}