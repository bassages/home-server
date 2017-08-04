package nl.wiegman.home.dev.klimaat;

import nl.wiegman.home.dev.energie.AbstractDataGeneratorService;
import nl.wiegman.home.klimaat.Klimaat;
import nl.wiegman.home.klimaat.KlimaatService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class KlimaatDummyDataGeneratorService extends AbstractDataGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(KlimaatDummyDataGeneratorService.class);

    public static final BigDecimal TEMPERATUUR_STEP = new BigDecimal(0.75d);
    public static final BigDecimal TEMPERATUUR_MINIMUM = new BigDecimal(10.0d);
    public static final BigDecimal TEMPERATUUR_MAXIMUM = new BigDecimal(32.0d);

    public static final BigDecimal LUCHTVOCHTIGHEID_STEP = new BigDecimal(0.75d);
    public static final BigDecimal LUCHTVOCHTIGHEID_MINIMUM = new BigDecimal(20.0d);
    public static final BigDecimal LUCHTVOCHTIGHEID_MAXIMUM = new BigDecimal(90.0d);

    public static final int GENERATOR_RUN_INTERVAL_IN_SECONDS = 10;

    private final ScheduledExecutorService historischeDataGeneratorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> dataGeneratorScheduler = null;

    private BigDecimal lastGeneratedTemperatuur = new BigDecimal(20.0d);
    private boolean temperatuurUp = true;

    private BigDecimal lastGeneratedLuchtvochtigheid = new BigDecimal(40.0d);
    private boolean luchtvochtigheidUp = true;

    @Value("${klimaatDataGenerator.autostart}")
    boolean autoStart;

    @Value("${klimaatDataGenerator.initialDelaySeconds}")
    int initialDelaySeconds;

    @Autowired
    private KlimaatService klimaatService;

    @PostConstruct
    public void init() {
        if (autoStart) {
            startGeneratingData();
        }
    }

    public void startGeneratingData() {
        if (dataGeneratorScheduler == null) {
            dataGeneratorScheduler = historischeDataGeneratorScheduler.scheduleAtFixedRate(this::generate, initialDelaySeconds, GENERATOR_RUN_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void generate() {
        try {
            Klimaat klimaat = new Klimaat();
            klimaat.setLuchtvochtigheid(getNextLuchtVochtigheid());
            klimaat.setTemperatuur(getNextTemperatuur());
            klimaat.setDatumtijd(new Date());
            klimaatService.add(klimaat);
        } catch (Throwable t) {  // Catch Throwable rather than Exception (a subclass).
            LOG.error("Caught exception in ScheduledExecutorService.", t);
        }
    }

    private BigDecimal getNextTemperatuur() {
        if (temperatuurUp) {
            if (lastGeneratedTemperatuur.compareTo(TEMPERATUUR_MAXIMUM) >= 0) {
                temperatuurUp = false;
            }
        } else {
            if (lastGeneratedTemperatuur.compareTo(TEMPERATUUR_MINIMUM) <= 0) {
                temperatuurUp = true;
            }
        }
        if (temperatuurUp) {
            lastGeneratedTemperatuur = lastGeneratedTemperatuur.add(TEMPERATUUR_STEP);
        } else {
            lastGeneratedTemperatuur = lastGeneratedTemperatuur.subtract(TEMPERATUUR_STEP);
        }
        return lastGeneratedTemperatuur;
    }

    private BigDecimal getNextLuchtVochtigheid() {
        if (luchtvochtigheidUp) {
            if (lastGeneratedLuchtvochtigheid.compareTo(LUCHTVOCHTIGHEID_MAXIMUM) >= 0) {
                luchtvochtigheidUp = false;
            }
        } else {
            if (lastGeneratedLuchtvochtigheid.compareTo(LUCHTVOCHTIGHEID_MINIMUM) <= 0) {
                luchtvochtigheidUp = true;
            }
        }
        if (luchtvochtigheidUp) {
            lastGeneratedLuchtvochtigheid = lastGeneratedLuchtvochtigheid.add(LUCHTVOCHTIGHEID_STEP);
        } else {
            lastGeneratedLuchtvochtigheid = lastGeneratedLuchtvochtigheid.subtract(LUCHTVOCHTIGHEID_STEP);
        }
        return lastGeneratedLuchtvochtigheid;
    }
}
