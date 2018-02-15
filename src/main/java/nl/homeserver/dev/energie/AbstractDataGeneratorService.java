package nl.homeserver.dev.energie;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractDataGeneratorService {

    static final int SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS = 10;
    static final BigDecimal INITIAL_GENERATOR_VALUE_STROOM = new BigDecimal("25000");
    static final BigDecimal INITIAL_GENERATOR_VALUE_GAS = new BigDecimal("15000");

    int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, 50 + 1);
        int max = ThreadLocalRandom.current().nextInt(50, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    BigDecimal getStroomIncreasePerInterval(LocalDateTime timestamp) {
        BigDecimal result;
        if (timestamp.getDayOfWeek() == DayOfWeek.SATURDAY || timestamp.getDayOfWeek() == DayOfWeek.SUNDAY) {
            result = new BigDecimal("0.0015");
        } else {
            result = new BigDecimal("0.001");
        }
        return result;
    }

    BigDecimal getGasIncreasePerInterval(LocalDateTime timestamp) {
        BigDecimal result = BigDecimal.ZERO;

        switch (timestamp.getMonth()) {
            case DECEMBER:
            case JANUARY:
            case FEBRUARY:
                result = new BigDecimal("0.0015");
                break;
            case MARCH:
            case APRIL:
                result = new BigDecimal("0.0005");
                break;
            case MAY:
            case JUNE:
            case JULY:
            case AUGUST:
                result = new BigDecimal("0.0001");
                break;
            case SEPTEMBER:
                result = new BigDecimal("0.0005d");
                break;
            case OCTOBER:
            case NOVEMBER:
                result = new BigDecimal("0.001d");
                break;
             default:
                 break;
        }
        return result;
    }
}
