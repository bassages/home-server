package nl.wiegman.home.klimaat;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

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

import nl.wiegman.home.DateTimeUtil;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatController {

    private static final String DEFAULT_KLIMAAT_SENSOR_CODE = "WOONKAMER";

    private final KlimaatService klimaatService;

    @Autowired
    public KlimaatController(KlimaatService klimaatService) {
        this.klimaatService = klimaatService;
    }

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

    @GetMapping
    public List<Klimaat> findAllInPeriod(@RequestParam("from") long from, @RequestParam("to") long to) {
        return klimaatService.getInPeriod(DEFAULT_KLIMAAT_SENSOR_CODE, new Date(from), new Date(to));
    }

    @GetMapping(path = "gemiddelde")
    public BigDecimal getAverage(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to) {
        return klimaatService.getAverage(SensorType.fromString(sensortype), new Date(from), new Date(to));
    }

    @GetMapping(path = "gemiddeld-per-maand-in-jaar")
    public List<List<GemiddeldeKlimaatPerMaandDto>> getAverage(@RequestParam("sensortype") String sensortype, @RequestParam("jaar") int[] jaren) {
        return IntStream.of(jaren).mapToObj(jaar ->
                IntStream.rangeClosed(1, 12)
                    .mapToObj(maand -> getAverageInMonthOfYear(sensortype, maand, jaar))
                    .collect(toList())).collect(toList());
    }

    private GemiddeldeKlimaatPerMaandDto getAverageInMonthOfYear(String sensortype, int maand, int jaar) {
        LocalDate from = LocalDate.of(jaar, maand, 1);
        LocalDate to = from.plusMonths(1);

        GemiddeldeKlimaatPerMaandDto gemiddeldeKlimaatPerMaandDto = new GemiddeldeKlimaatPerMaandDto();
        gemiddeldeKlimaatPerMaandDto.setMaand(DateTimeUtil.toDate(from));
        gemiddeldeKlimaatPerMaandDto.setGemiddelde(klimaatService.getAverage(SensorType.fromString(sensortype), DateTimeUtil.toDate(from), DateTimeUtil.toDate(to)));
        return gemiddeldeKlimaatPerMaandDto;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleBadRequests(HttpServletResponse response, IllegalArgumentException ex) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

}
