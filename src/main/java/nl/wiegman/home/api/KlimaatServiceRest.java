package nl.wiegman.home.api;

import nl.wiegman.home.api.dto.KlimaatDTO;
import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.model.KlimaatSensor;
import nl.wiegman.home.model.SensorType;
import nl.wiegman.home.service.KlimaatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatServiceRest {

    @Autowired
    private KlimaatService klimaatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody KlimaatDTO klimaat) {
        klimaatService.add(mapDtoToKlimaat(klimaat));
    }

    @GetMapping(path = "meest-recente")
    public Klimaat getMostRecent() {
        return klimaatService.getMostRecent();
    }

    @GetMapping(path = "hoogste")
    public List<Klimaat> getHighest(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
        return klimaatService.getHighest(SensorType.fromString(sensortype), new Date(from), new Date(to), limit);
    }

    @GetMapping(path = "laagste")
    public List<Klimaat> getLowest(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
        return klimaatService.getLowest(SensorType.fromString(sensortype), new Date(from), new Date(to), limit);
    }

    @GetMapping("get/{from}/{to}")
    public List<Klimaat> get(@PathVariable("from") long from, @PathVariable("to") long to) {
        return klimaatService.getInPeriod(new Date(from), new Date(to));
    }

    @GetMapping(path = "gemiddelde")
    public BigDecimal getAverage(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to) {
        return klimaatService.getAverage(SensorType.fromString(sensortype), new Date(from), new Date(to));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleBadRequests(HttpServletResponse response, IllegalArgumentException ex) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    private Klimaat mapDtoToKlimaat(KlimaatDTO klimaat) {
        KlimaatSensor klimaatSensor = klimaatService.getKlimaatSensorByCode(klimaat.getKlimaatSensorCode());
        if (klimaatSensor == null) {
            throw new IllegalArgumentException(String.format("klimaatsensor with code %s does not exist", klimaat.getKlimaatSensorCode()));
        }
        Klimaat klimaatToSave = new Klimaat();
        klimaatToSave.setDatumtijd(klimaat.getDatumtijd());
        klimaatToSave.setTemperatuur(klimaat.getTemperatuur());
        klimaatToSave.setLuchtvochtigheid(klimaat.getLuchtvochtigheid());
        klimaatToSave.setKlimaatSensor(klimaatSensor);
        return klimaatToSave;
    }

}
