package nl.homeserver.climate;

import lombok.RequiredArgsConstructor;
import nl.homeserver.ResourceNotFoundException;
import nl.homeserver.config.Paths;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static nl.homeserver.DatePeriod.aPeriodWithToDate;
import static nl.homeserver.climate.SensorType.toSensorType;

@RestController
@RequestMapping(Paths.API + "/klimaat")
@RequiredArgsConstructor
class KlimaatController {

    private final KlimaatService klimaatService;
    private final KlimaatSensorService klimaatSensorService;
    private final IncomingKlimaatService incomingKlimaatService;

    @GetMapping("sensors")
    public List<KlimaatSensor> getAllKlimaatSensors() {
        return klimaatSensorService.getAll();
    }

    @PostMapping("sensors/{sensorCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable("sensorCode") final String sensorCode, @RequestBody final KlimaatDto klimaatDto) {
        final KlimaatSensor klimaatSensor = getKlimaatSensorExpectingOne(sensorCode);

        final Klimaat klimaat = new Klimaat();
        klimaat.setDatumtijd(klimaatDto.datumtijd());
        klimaat.setLuchtvochtigheid(klimaatDto.luchtvochtigheid());
        klimaat.setTemperatuur(klimaatDto.temperatuur());
        klimaat.setKlimaatSensor(klimaatSensor);

        incomingKlimaatService.add(klimaat);
    }

    @PutMapping("sensors/{sensorCode}")
    public KlimaatSensor update(@PathVariable("sensorCode") final String sensorCode,
                                @RequestBody final KlimaatSensorDto klimaatSensorDto) {
        final KlimaatSensor existingKlimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        existingKlimaatSensor.setOmschrijving(klimaatSensorDto.getOmschrijving());
        return klimaatSensorService.save(existingKlimaatSensor);
    }

    @DeleteMapping("sensors/{sensorCode}")
    public void delete(@PathVariable("sensorCode") final String sensorCode) {
        final KlimaatSensor existingKlimaatSensor = getKlimaatSensorExpectingOne(sensorCode);
        klimaatService.deleteByKlimaatSensor(existingKlimaatSensor);
        klimaatSensorService.delete(existingKlimaatSensor);
    }

    @GetMapping(path = "{sensorCode}/meest-recente")
    public RealtimeKlimaat getMostRecent(@PathVariable("sensorCode") final String sensorCode) {
        getKlimaatSensorExpectingOne(sensorCode);
        return incomingKlimaatService.getMostRecent(sensorCode);
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
        return klimaatSensorService.getByCode(klimaatSensorCode)
                                   .orElseThrow(() -> new ResourceNotFoundException("KlimaatSensor", klimaatSensorCode));
    }
}
