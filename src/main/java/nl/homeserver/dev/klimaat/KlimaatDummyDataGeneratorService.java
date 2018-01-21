package nl.homeserver.dev.klimaat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nl.homeserver.dev.energie.AbstractDataGeneratorService;
import nl.homeserver.klimaat.Klimaat;
import nl.homeserver.klimaat.KlimaatSensorRepository;
import nl.homeserver.klimaat.KlimaatService;

@Service
public class KlimaatDummyDataGeneratorService extends AbstractDataGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(KlimaatDummyDataGeneratorService.class);

    private static final BigDecimal TEMPERATUUR_STEP = new BigDecimal("0.75");
    private static final BigDecimal TEMPERATUUR_MINIMUM = new BigDecimal("10.0");
    private static final BigDecimal TEMPERATUUR_MAXIMUM = new BigDecimal("32.0");

    private static final BigDecimal LUCHTVOCHTIGHEID_STEP = new BigDecimal("0.75");
    private static final BigDecimal LUCHTVOCHTIGHEID_MINIMUM = new BigDecimal("20.0");
    private static final BigDecimal LUCHTVOCHTIGHEID_MAXIMUM = new BigDecimal("90.0");

    private static final int GENERATOR_RUN_INTERVAL_IN_SECONDS = 10;

    private final ScheduledExecutorService historischeDataGeneratorScheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> dataGeneratorScheduler = null;

    private BigDecimal lastGeneratedTemperatuur = new BigDecimal("20.0");
    private boolean temperatuurUp = true;

    private BigDecimal lastGeneratedLuchtvochtigheid = new BigDecimal("40.0");
    private boolean luchtvochtigheidUp = true;

    @Value("${klimaatDataGenerator.autostart}")
    boolean autoStart;

    @Value("${klimaatDataGenerator.initialDelaySeconds}")
    int initialDelaySeconds;

    private final KlimaatService klimaatService;
    private final KlimaatSensorRepository klimaatSensorRepository;

    public KlimaatDummyDataGeneratorService(KlimaatService klimaatService, KlimaatSensorRepository klimaatSensorRepository) {
        this.klimaatService = klimaatService;
        this.klimaatSensorRepository = klimaatSensorRepository;
    }

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
            klimaat.setDatumtijd(LocalDateTime.now());
            klimaat.setKlimaatSensor(klimaatSensorRepository.findAll().get(0));
            klimaatService.add(klimaat);
        } catch (Exception e) {
            LOG.error("Caught exception in ScheduledExecutorService.", e);
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
