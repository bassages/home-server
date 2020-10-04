package nl.homeserver.klimaat;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.klimaat.SensorType.toSensorType;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import nl.homeserver.ResourceNotFoundException;
import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/klimaat")
@AllArgsConstructor
class KlimaatController {

    private final KlimaatService klimaatService;

    @GetMapping("sensors")
    public List<KlimaatSensor> getAllKlimaatSensors() {
        return klimaatService.getAllKlimaatSensors();
    }

    @PostMapping("sensors/{sensorCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable("sensorCode") final String sensorCode, @RequestBody final KlimaatDto klimaatDto) {
        final KlimaatSensor klimaatSensor = getKlimaatSensorExpectingOne(sensorCode);

        final Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(klimaatDto.getDatumtijd());
        klimaat.setLuchtvochtigheid(klimaatDto.getLuchtvochtigheid());
        klimaat.setTemperatuur(klimaatDto.getTemperatuur());
        klimaat.setKlimaatSensor(klimaatSensor);

        klimaatService.add(klimaat);
    }

    @PutMapping("sensors/{sensorCode}")
    public KlimaatSensor update(@PathVariable("sensorCode") final String sensorCode, @RequestBody final KlimaatSensorDto klimaatSensorDto) {
        final KlimaatSensor existingKlimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        existingKlimaatSensor.setOmschrijving(klimaatSensorDto.getOmschrijving());
        return klimaatService.update(existingKlimaatSensor);
    }

    @DeleteMapping("sensors/{sensorCode}")
    public void delete(@PathVariable("sensorCode") final String sensorCode) {
        final KlimaatSensor existingKlimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        klimaatService.delete(existingKlimaatSensor);
    }

    @GetMapping(path = "{sensorCode}/meest-recente")
    public RealtimeKlimaat getMostRecent(@PathVariable("sensorCode") final String sensorCode) {
        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getMostRecent(sensorCode);
    }

    @GetMapping(path = "{sensorCode}/hoogste")
    public List<Klimaat> getHighest(
            @PathVariable("sensorCode") final String sensorCode,
            @RequestParam("sensorType") final String sensorType,
            @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) final LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) final LocalDate to,
            @RequestParam("limit") final int limit) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getHighest(sensorCode, toSensorType(sensorType), aPeriodWithToDate(from, to), limit);
    }

    @GetMapping(path = "{sensorCode}/laagste")
    public List<Klimaat> getLowest(
            @PathVariable("sensorCode") final String sensorCode,
            @RequestParam("sensorType") final String sensorType,
            @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) final LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) final LocalDate to,
            @RequestParam("limit") final int limit) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getLowest(sensorCode, toSensorType(sensorType), aPeriodWithToDate(from, to), limit);
    }

    @GetMapping(path = "{sensorCode}")
    public List<Klimaat> findAllInPeriod(
            @PathVariable("sensorCode") final String sensorCode,
            @RequestParam("from") @DateTimeFormat(iso = ISO.DATE) final LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = ISO.DATE) final LocalDate to) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getInPeriod(sensorCode, aPeriodWithToDate(from, to));
    }

    @GetMapping(path = "{sensorCode}/gemiddeld-per-maand-in-jaar")
    public List<List<GemiddeldeKlimaatPerMaand>> getAverage(
            @PathVariable("sensorCode") final String sensorCode,
            @RequestParam("sensorType") final String sensorType,
            @RequestParam("jaar") final int[] years) {

        getKlimaatSensorExpectingOne(sensorCode);
        return klimaatService.getAveragePerMonthInYears(sensorCode, toSensorType(sensorType), years);
    }

    private KlimaatSensor getKlimaatSensorExpectingOne(final String klimaatSensorCode) {
        return klimaatService.getKlimaatSensorByCode(klimaatSensorCode)
                             .orElseThrow(() -> new ResourceNotFoundException("KlimaatSensor", klimaatSensorCode));
    }
}
