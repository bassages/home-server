package nl.wiegman.home.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.model.KlimaatSensor;
import nl.wiegman.home.model.SensorType;
import nl.wiegman.home.service.KlimaatService;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatServiceRest {

    @Autowired
    private KlimaatService klimaatService;

    @PostMapping("sensors/{sensorCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@PathVariable("sensorCode") String sensorCode, @RequestBody Klimaat klimaat) {
        KlimaatSensor klimaatSensor = klimaatService.getKlimaatSensorByCode(sensorCode);
        if (klimaatSensor == null) {
            throw new IllegalArgumentException(String.format("klimaatsensor with code %s does not exist", sensorCode));
        } else {
            klimaat.setKlimaatSensor(klimaatSensor);
        }
        klimaatService.add(klimaat);
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

}
