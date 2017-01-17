package nl.wiegman.home.api;

import nl.wiegman.home.model.Klimaat;
import nl.wiegman.home.model.SensorType;
import nl.wiegman.home.service.KlimaatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatServiceRest {

    @Autowired
    private KlimaatService klimaatService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody Klimaat klimaat) {
        klimaatService.add(klimaat);
    }

    @GetMapping(path = "meest-recente")
    public Klimaat getMostRecent() {
        return klimaatService.getMostRecent();
    }

    @GetMapping(path = "hoogste")
    public List<Klimaat> getHighest(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
        return klimaatService.getHighest(sensortype, new Date(from), new Date(to), limit);
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
}
