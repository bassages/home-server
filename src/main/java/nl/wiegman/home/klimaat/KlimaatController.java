package nl.wiegman.home.klimaat;

import static java.util.stream.Collectors.toList;
import static nl.wiegman.home.DatePeriod.aPeriodWithToDate;
import static nl.wiegman.home.DateTimeUtil.toDateAtStartOfDay;
import static nl.wiegman.home.DateTimeUtil.toLocalDate;
import static nl.wiegman.home.DateTimeUtil.toLocalDateTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import nl.wiegman.home.DatePeriod;
import nl.wiegman.home.DateTimePeriod;

@RestController
@RequestMapping("/api/klimaat")
public class KlimaatController {

    public static final String DEFAULT_KLIMAAT_SENSOR_CODE = "WOONKAMER";

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
    public RealtimeKlimaat getMostRecent() {
        return klimaatService.getMostRecent(DEFAULT_KLIMAAT_SENSOR_CODE);
    }

    @GetMapping(path = "hoogste")
    public List<Klimaat> getHighest(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
        DatePeriod period = aPeriodWithToDate(toLocalDate(from), toLocalDate(to));
        return klimaatService.getHighest(SensorType.fromString(sensortype), period, limit);
    }

    @GetMapping(path = "laagste")
    public List<Klimaat> getLowest(@RequestParam("sensortype") String sensortype, @RequestParam("from") long from, @RequestParam("to") long to, @RequestParam("limit") int limit) {
        DatePeriod period = aPeriodWithToDate(toLocalDate(from), toLocalDate(to));
        return klimaatService.getLowest(SensorType.fromString(sensortype), period, limit);
    }

    @GetMapping
    public List<Klimaat> findAllInPeriod(@RequestParam("from") long start, @RequestParam("to") long end) {
        DateTimePeriod period = DateTimePeriod.aPeriodWithEndDateTime(toLocalDateTime(start), toLocalDateTime(end));
        return klimaatService.getInPeriod(DEFAULT_KLIMAAT_SENSOR_CODE, period);
    }

    @GetMapping(path = "gemiddeld-per-maand-in-jaar")
    public List<List<GemiddeldeKlimaatPerMaand>> getAverage(@RequestParam("sensortype") String sensortype, @RequestParam("jaar") int[] jaren) {
        return IntStream.of(jaren).mapToObj(jaar ->
                IntStream.rangeClosed(1, Month.values().length)
                    .mapToObj(maand -> getAverageInMonthOfYear(sensortype, YearMonth.of(jaar, maand)))
                    .collect(toList())).collect(toList());
    }

    private GemiddeldeKlimaatPerMaand getAverageInMonthOfYear(String sensortype, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = from.plusMonths(1);

        BigDecimal average = klimaatService.getAverage(SensorType.fromString(sensortype), aPeriodWithToDate(from, to));

        return new GemiddeldeKlimaatPerMaand(toDateAtStartOfDay(from), average);
    }

    // TODO: this causes stack traces to be "swallowed"
//    @ExceptionHandler(IllegalArgumentException.class)
//    public void handleBadRequests(HttpServletResponse response, IllegalArgumentException ex) throws IOException {
//        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
//    }

}
