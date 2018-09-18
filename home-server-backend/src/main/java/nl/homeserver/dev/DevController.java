package nl.homeserver.dev;

import static java.time.temporal.ChronoUnit.HOURS;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.config.Paths;
import nl.homeserver.dev.energie.SlimmeMeterSimulatorService;
import nl.homeserver.energie.MeterstandHousekeeping;
import nl.homeserver.energie.OpgenomenVermogenHousekeeping;
import nl.homeserver.klimaat.Klimaat;
import nl.homeserver.klimaat.KlimaatRepos;
import nl.homeserver.klimaat.KlimaatSensor;
import nl.homeserver.klimaat.KlimaatSensorRepository;

@RestController
@RequestMapping(Paths.API + "/dev")
public class DevController {

    private final KlimaatRepos klimaatRepos;
    private final KlimaatSensorRepository klimaatSensorRepository;
    private final SlimmeMeterSimulatorService slimmeMeterSimulatorService;

    private final MeterstandHousekeeping meterstandHousekeeping;
    private final OpgenomenVermogenHousekeeping opgenomenVermogenHousekeeping;

    public DevController(KlimaatRepos klimaatRepos, KlimaatSensorRepository klimaatSensorRepository, SlimmeMeterSimulatorService slimmeMeterSimulatorService, MeterstandHousekeeping meterstandHousekeeping, OpgenomenVermogenHousekeeping opgenomenVermogenHousekeeping) {
        this.klimaatRepos = klimaatRepos;
        this.klimaatSensorRepository = klimaatSensorRepository;
        this.slimmeMeterSimulatorService = slimmeMeterSimulatorService;
        this.meterstandHousekeeping = meterstandHousekeeping;
        this.opgenomenVermogenHousekeeping = opgenomenVermogenHousekeeping;
    }

    @GetMapping(path = "startHousekeeping")
    public String startHousekeeping() {
        meterstandHousekeeping.start();
        opgenomenVermogenHousekeeping.start();
        return "Housekeeping completed";
    }

    @GetMapping(path = "generateKlimaatDummyData")
    public String generateKlimaatDummyData() {
        klimaatRepos.deleteAll();

        klimaatSensorRepository.deleteAll();

        KlimaatSensor klimaatSensor = new KlimaatSensor();
        klimaatSensor.setCode("WOONKAMER");
        klimaatSensor.setOmschrijving("Huiskamer");
        klimaatSensorRepository.save(klimaatSensor);

        LocalDateTime toDateTime = LocalDateTime.now().truncatedTo(HOURS);

        LocalDateTime loopDateTime = toDateTime.minusDays(400);

        while (loopDateTime.isBefore(toDateTime)) {
            loopDateTime = loopDateTime.plusMinutes(15);

            Klimaat klimaat = new Klimaat();
            klimaat.setDatumtijd(loopDateTime);
            klimaat.setKlimaatSensor(klimaatSensor);

            long dayOfMonth = Long.parseLong(new SimpleDateFormat("d").format(loopDateTime));
            long hourOfDay = Long.parseLong(new SimpleDateFormat("HH").format(loopDateTime));

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