package nl.wiegman.home.klimaat;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

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
        List<Pair<Date, BigDecimal>> validSensorValues = klimaats.stream()
                .filter(klimaat -> nonNull(sensorValueGetter.apply(klimaat)))
                .map(klimaat -> Pair.of(klimaat.getDatumtijd(), sensorValueGetter.apply(klimaat)))
                .collect(toList());

        if (isNotEmpty(validSensorValues) && validSensorValues.size() >= MINIMUM_NUMBER_OF_ITEMS_TO_DETERMINE_TREND) {
            BigDecimal slope = calculateSlope(validSensorValues);
            return SIGNUM_OF_SLOPE_TO_TREND_MAPPING.get(slope.signum());
        }
        return null;
    }

    private BigDecimal calculateSlope(List<Pair<Date, BigDecimal>> sensorValues) {
        SimpleRegression simpleRegression = new SimpleRegression();

        sensorValues.forEach(sensorValue -> {
            simpleRegression.addData(sensorValue.getKey().getTime(), sensorValue.getValue().doubleValue());
        });

        return new BigDecimal(simpleRegression.getSlope());
    }
}