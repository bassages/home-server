package nl.homeserver.klimaat;

import static java.lang.String.format;
import static nl.homeserver.DatePeriod.aPeriodWithToDate;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.DatePeriod;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatController {

    public static final String DEFAULT_KLIMAAT_SENSOR_CODE = "WOONKAMER";

    private final KlimaatService klimaatService;

    public KlimaatController(KlimaatService klimaatService) {
        this.klimaatService = klimaatService;
    }

    @PostMapping("sensors/{sensorCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable("sensorCode") String sensorCode, @RequestBody Klimaat klimaat) {
        KlimaatSensor klimaatSensor = klimaatService.getKlimaatSensorByCode(sensorCode)
                                                    .orElseThrow(() -> new IllegalArgumentException(format("klimaatsensor with code %s does not exist", sensorCode)));
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaatService.add(klimaat);
    }

    @GetMapping(path = "meest-recente")
    public RealtimeKlimaat getMostRecent() {
        return klimaatService.getMostRecent(DEFAULT_KLIMAAT_SENSOR_CODE);
    }

    @GetMapping(path = "hoogste")
    public List<Klimaat> getHighest(@RequestParam("sensortype") String sensortype,
                                    @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                    @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
                                    @RequestParam("limit") int limit) {

        DatePeriod period = aPeriodWithToDate(from, to);
        return klimaatService.getHighest(SensorType.fromString(sensortype), period, limit);
    }

    @GetMapping(path = "laagste")
    public List<Klimaat> getLowest(@RequestParam("sensortype") String sensortype,
                                   @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                   @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
                                   @RequestParam("limit") int limit) {

        DatePeriod period = aPeriodWithToDate(from, to);
        return klimaatService.getLowest(SensorType.fromString(sensortype), period, limit);
    }

    @GetMapping
    public List<Klimaat> findAllInPeriod(@RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                         @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

        DatePeriod period = aPeriodWithToDate(from, to);
        return klimaatService.getInPeriod(DEFAULT_KLIMAAT_SENSOR_CODE, period);
    }

    @GetMapping(path = "gemiddeld-per-maand-in-jaar")
    public List<List<GemiddeldeKlimaatPerMaand>> getAverage(@RequestParam("sensortype") String sensortype, @RequestParam("jaar") int[] jaren) {
        return klimaatService.getAverage(SensorType.fromString(sensortype), jaren);
    }
}
