package nl.homeserver.energie;

import static java.math.RoundingMode.HALF_UP;
import static nl.homeserver.DateTimeUtil.toLocalDateTime;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slimmemeter")
public class SlimmeMeterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlimmeMeterController.class);

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final MeterstandService meterstandService;

    private static final int GAS_SCALE = 3;
    private static final int STROOM_SCALE = 3;

    public SlimmeMeterController(OpgenomenVermogenService opgenomenVermogenService, MeterstandService meterstandService) {
        this.opgenomenVermogenService = opgenomenVermogenService;
        this.meterstandService = meterstandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@RequestBody Dsmr42Reading dsmr42Reading) {
        LOGGER.info(ReflectionToStringBuilder.toString(dsmr42Reading, new RecursiveToStringStyle()));

        saveMeterstand(dsmr42Reading);
        saveOpgenomenVermogen(dsmr42Reading);
    }

    private void saveMeterstand(@RequestBody Dsmr42Reading dsmr42Reading) {
        Meterstand meterstand = mapToMeterStand(dsmr42Reading);
        meterstandService.save(meterstand);
    }

    private void saveOpgenomenVermogen(@RequestBody Dsmr42Reading dsmr42Reading) {
        OpgenomenVermogen opgenomenVermogen = mapToOpgenomenVermogen(dsmr42Reading);
        opgenomenVermogenService.save(opgenomenVermogen);
    }

    private Meterstand mapToMeterStand(Dsmr42Reading dsmr42Reading) {
        Meterstand meterstand = new Meterstand();
        meterstand.setDateTime(toLocalDateTime(dsmr42Reading.getDatumtijd()));
        meterstand.setStroomTariefIndicator(StroomTariefIndicator.byId(dsmr42Reading.getStroomTariefIndicator().shortValue()));
        meterstand.setGas(dsmr42Reading.getGas().setScale(GAS_SCALE, HALF_UP));
        meterstand.setStroomTarief1(dsmr42Reading.getStroomTarief1().setScale(STROOM_SCALE, HALF_UP));
        meterstand.setStroomTarief2(dsmr42Reading.getStroomTarief2().setScale(STROOM_SCALE, HALF_UP));
        return meterstand;
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(Dsmr42Reading dsmr42Reading) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(toLocalDateTime(dsmr42Reading.getDatumtijd()));
        opgenomenVermogen.setWatt(dsmr42Reading.getStroomOpgenomenVermogenInWatt());
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.byId(dsmr42Reading.getStroomTariefIndicator().shortValue()));
        return opgenomenVermogen;
    }
}
