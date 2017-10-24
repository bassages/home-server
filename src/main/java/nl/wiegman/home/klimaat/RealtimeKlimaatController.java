package nl.wiegman.home.klimaat;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import nl.wiegman.home.UpdateEvent;

@Controller
public class RealtimeKlimaatController {

    private static final String TOPIC = "/topic/klimaat";

    private static final int MINIMUM_HISTORY_SIZE_TO_DETERMINE_TREND = 3;

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, List<Klimaat>> latestKlimaatPerSensorCode = new HashMap<>();

    private static final Map<Integer, Trend> SIGNUM_OF_SLOPE_TO_TREN_MAPPING = new HashMap<Integer, Trend>() {
        {
            put(-1, Trend.DOWN);
            put(0, Trend.STABLE);
            put(1, Trend.UP);
        }
    };

    @Autowired
    public RealtimeKlimaatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onApplicationEvent(UpdateEvent event) {
        Object updatedObject = event.getUpdatedObject();
        if (updatedObject instanceof Klimaat) {
            Klimaat updatedKlimaat = (Klimaat) updatedObject;
            latestKlimaatPerSensorCode.computeIfAbsent(updatedKlimaat.getKlimaatSensor().getCode(), klimaatSensorCode -> new ArrayList<>()).add(updatedKlimaat);

            RealtimeKlimaat realtimeKlimaat = new RealtimeKlimaat();
            realtimeKlimaat.setDatumtijd(updatedKlimaat.getDatumtijd());
            realtimeKlimaat.setLuchtvochtigheid(updatedKlimaat.getLuchtvochtigheid());
            realtimeKlimaat.setTemperatuur(updatedKlimaat.getTemperatuur());
            realtimeKlimaat.setTemperatuurTrend(determineTemperatuurTrend(updatedKlimaat.getKlimaatSensor()));

            messagingTemplate.convertAndSend(TOPIC, realtimeKlimaat);
        }
    }

    private Trend determineTemperatuurTrend(KlimaatSensor klimaatSensor) {
        List<Klimaat> latestKlimaatsForKlimaatSensor = latestKlimaatPerSensorCode.get(klimaatSensor.getCode());

        cleanupOld(latestKlimaatsForKlimaatSensor);

        if (isNotEmpty(latestKlimaatsForKlimaatSensor) && latestKlimaatsForKlimaatSensor.size() >= MINIMUM_HISTORY_SIZE_TO_DETERMINE_TREND) {
            BigDecimal slope = calculateSlope(latestKlimaatsForKlimaatSensor);
            return SIGNUM_OF_SLOPE_TO_TREN_MAPPING.get(slope.signum());
        }

        return null;
    }

    private void cleanupOld(List<Klimaat> latestKlimaatsForKlimaatSensor) {
        Klimaat mostRecent = latestKlimaatsForKlimaatSensor.get(latestKlimaatsForKlimaatSensor.size()-1);

        latestKlimaatsForKlimaatSensor.removeIf(klimaat -> {
            long diffInMs = mostRecent.getDatumtijd().getTime() - klimaat.getDatumtijd().getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMs);
            return diffInMinutes >= 10;
        });
    }

    private BigDecimal calculateSlope(List<Klimaat> klimaats) {
        SimpleRegression simpleRegression = new SimpleRegression();

        klimaats.forEach(klimaat -> {
            simpleRegression.addData(klimaat.getDatumtijd().getTime(), klimaat.getTemperatuur().doubleValue());
        });

        return new BigDecimal(simpleRegression.getSlope());
    }
}