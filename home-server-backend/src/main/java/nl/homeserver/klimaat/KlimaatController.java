package nl.homeserver.klimaat;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.SensorType.toSensorType;

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

import nl.homeserver.ResourceNotFoundException;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatController {

    private final KlimaatService klimaatService;

    public KlimaatController(KlimaatService klimaatService) {
        this.klimaatService = klimaatService;
    }

    @GetMapping("sensors")
    public List<KlimaatSensor> getAllKlimaatSensors() {
        return klimaatService.getAllKlimaatSensors();
    }

    @PostMapping("sensors/{sensorCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable("sensorCode") String sensorCode, @RequestBody Klimaat klimaat) {
        KlimaatSensor klimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        klimaat.setKlimaatSensor(klimaatSensor);
        klimaatService.add(klimaat);
    }

    @GetMapping(path = "{sensorCode}/meest-recente")
    public RealtimeKlimaat getMostRecent(@PathVariable("sensorCode") String sensorCode) {
        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getMostRecent(sensorCode);
    }

    @GetMapping(path = "{sensorCode}/hoogste")
    public List<Klimaat> getHighest(@PathVariable("sensorCode") String sensorCode,
                                    @RequestParam("sensorType") String sensorType,
                                    @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                    @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
                                    @RequestParam("limit") int limit) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getHighest(sensorCode, toSensorType(sensorType), aPeriodWithToDate(from, to), limit);
    }

    @GetMapping(path = "{sensorCode}/laagste")
    public List<Klimaat> getLowest(@PathVariable("sensorCode") String sensorCode,
                                   @RequestParam("sensorType") String sensorType,
                                   @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                   @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to,
                                   @RequestParam("limit") int limit) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getLowest(sensorCode, toSensorType(sensorType), aPeriodWithToDate(from, to), limit);
    }

    @GetMapping(path = "{sensorCode}")
    public List<Klimaat> findAllInPeriod(@PathVariable("sensorCode") String sensorCode,
                                         @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) LocalDate from,
                                         @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) LocalDate to) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getInPeriod(sensorCode, aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "{sensorCode}/gemiddeld-per-maand-in-jaar")
    public List<List<GemiddeldeKlimaatPerMaand>> getAverage(@PathVariable("sensorCode") String sensorCode,
                                                            @RequestParam("sensorType") String sensorType, @RequestParam("jaar") int[] jaren) {
        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getAveragePerMonthInYears(sensorCode, toSensorType(sensorType), jaren);
    }

    private KlimaatSensor getKlimaatSensorExpectingOne(@PathVariable("klimaatSensorCode") String klimaatSensorCode) {
        return klimaatService.getKlimaatSensorByCode(klimaatSensorCode)
                             .orElseThrow(() -> new ResourceNotFoundException("KlimaatSensor", klimaatSensorCode));
    }
}
