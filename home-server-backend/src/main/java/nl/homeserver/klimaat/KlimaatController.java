package nl.homeserver.klimaat;

import nl.homeserver.ResourceNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.SensorType.toSensorType;

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

    @PutMapping("sensors/{sensorCode}")
    public KlimaatSensor update(@PathVariable("sensorCode") String sensorCode, @RequestBody KlimaatSensor klimaatSensor) {
        KlimaatSensor existingKlimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        existingKlimaatSensor.setOmschrijving(klimaatSensor.getOmschrijving());
        return klimaatService.update(existingKlimaatSensor);
    }

    @DeleteMapping("sensors/{sensorCode}")
    public void delete(@PathVariable("sensorCode") String sensorCode) {
        KlimaatSensor existingKlimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        klimaatService.delete(existingKlimaatSensor);
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

    private KlimaatSensor getKlimaatSensorExpectingOne(String klimaatSensorCode) {
        return klimaatService.getKlimaatSensorByCode(klimaatSensorCode)
                             .orElseThrow(() -> new ResourceNotFoundException("KlimaatSensor", klimaatSensorCode));
    }
}
