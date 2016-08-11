package nl.wiegman.home.service.dummydatagenerator.energie;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractDataGeneratorService {

    public static final int SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS = 10;
    public static final BigDecimal INITIAL_GENERATOR_VALUE_STROOM = new BigDecimal(25000d);
    public static final BigDecimal INITIAL_GENERATOR_VALUE_GAS = new BigDecimal(15000d);

    protected int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, 50 + 1);
        int max = ThreadLocalRandom.current().nextInt(50, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    protected BigDecimal getStroomIncreasePerInterval(long timestamp) {
        BigDecimal result;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        if (calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
            result = new BigDecimal(0.0015d);
        } else {
            result = new BigDecimal(0.001d);
        }
        return result;
    }

    protected BigDecimal getGasIncreasePerInterval(long timestamp) {
        BigDecimal result = BigDecimal.ZERO;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        switch (calendar.get(Calendar.MONTH)) {
            case Calendar.DECEMBER:
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
                result = new BigDecimal(0.0015d);
                break;
            case Calendar.MARCH:
            case Calendar.APRIL:
                result = new BigDecimal(0.0005d);
                break;
            case Calendar.MAY:
            case Calendar.JUNE:
            case Calendar.JULY:
            case Calendar.AUGUST:
                result = new BigDecimal(0.0001d);
                break;
            case Calendar.SEPTEMBER:
                result = new BigDecimal(0.0005d);
                break;
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
                result = new BigDecimal(0.001d);
                break;
        }
        return result;
    }
}
