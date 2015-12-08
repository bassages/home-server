package nl.wiegman.homecontrol.services.service.datagenerator;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractDataGeneratorService {

    public static final int SLIMME_METER_UPDATE_INTERVAL_IN_SECONDS = 10;
    public static final double STROOM_VERBRUIK_PER_INTERVAL = 0.001d;
    public static final double INITIAL_GENERATOR_VALUE_STROOM = 100000d;

    private int lastGeneratedOpgenomenVermogen = 50;

    protected int getDummyVermogenInWatt() {
        int min = ThreadLocalRandom.current().nextInt(50, lastGeneratedOpgenomenVermogen + 1);
        int max = ThreadLocalRandom.current().nextInt(lastGeneratedOpgenomenVermogen, 1200 + 1);
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
