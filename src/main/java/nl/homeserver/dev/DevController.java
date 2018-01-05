package nl.homeserver.dev;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.DateTimeUtil;
import nl.homeserver.dev.energie.SlimmeMeterSimulatorService;
import nl.homeserver.klimaat.Klimaat;
import nl.homeserver.klimaat.KlimaatRepos;
import nl.homeserver.klimaat.KlimaatSensor;
import nl.homeserver.klimaat.KlimaatSensorRepository;

@RestController
@RequestMapping("/api/dev")
public class DevController {

    private final KlimaatRepos klimaatRepos;
    private final KlimaatSensorRepository klimaatSensorRepository;
    private final SlimmeMeterSimulatorService slimmeMeterSimulatorService;

    public DevController(KlimaatRepos klimaatRepos, KlimaatSensorRepository klimaatSensorRepository, SlimmeMeterSimulatorService slimmeMeterSimulatorService) {
        this.klimaatRepos = klimaatRepos;
        this.klimaatSensorRepository = klimaatSensorRepository;
        this.slimmeMeterSimulatorService = slimmeMeterSimulatorService;
    }

    @GetMapping(path = "generateKlimaatDummyData")
    public String generateKlimaatDummyData() {
        klimaatRepos.deleteAll();

        klimaatSensorRepository.deleteAll();

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("WOONKAMER");
        klimaatSensor.setOmschrijving("Huiskamer");
        klimaatSensorRepository.save(klimaatSensor);

        Date date = new Date();
        date = DateUtils.truncate(date, Calendar.HOUR);

        Date toDate = (Date) date.clone();
        toDate = DateUtils.addDays(toDate, -400); // Generate at least 2 months of data

        while (date.after(toDate)) {
            date = DateUtils.addMinutes(date, -15);
            Klimaat klimaat = new Klimaat();
            klimaat.setDatumtijd(DateTimeUtil.toLocalDateTime(date));
            klimaat.setKlimaatSensor(klimaatSensor);

            long dayOfMonth = Long.valueOf(new SimpleDateFormat("d").format(date));
            long hourOfDay = Long.valueOf(new SimpleDateFormat("HH").format(date));

            klimaat.setTemperatuur(new BigDecimal(dayOfMonth + "." + hourOfDay));
            klimaat.setLuchtvochtigheid(new BigDecimal(((dayOfMonth + 45) + "." + hourOfDay)));

            klimaatRepos.save(klimaat);
        }

        return "Done";
    }

    @PostMapping(path = "startSlimmeMeterSimulator")
    public void startSlimmeMeterSimulator() {
        slimmeMeterSimulatorService.startSlimmeMeterSimulator();
    }

    @PostMapping(path = "stopSlimmeMeterSimulator")
    public void stopSlimmeMeterSimulator() {
        slimmeMeterSimulatorService.stopSlimmeMeterSimulator();
    }
}