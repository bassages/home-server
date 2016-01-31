package nl.wiegman.homecontrol.services.service.dummydatagenerator;

import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractDataGeneratorService {

    public static final int SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS = 10;
    public static final double INITIAL_GENERATOR_VALUE_STROOM = 100000d;

    protected int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, 50 + 1);
        int max = ThreadLocalRandom.current().nextInt(50, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    protected double getStroomInterval(long timestamp) {
        double result;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        if (calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
            result = 0.0015d;
        } else {
            result = 0.001d;
        }
        return result;
    }
}
