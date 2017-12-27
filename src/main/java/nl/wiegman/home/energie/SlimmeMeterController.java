package nl.wiegman.home.energie;

import static java.math.RoundingMode.HALF_UP;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.wiegman.home.UpdateEvent;

@RestController
@RequestMapping("/api/slimmemeter")
public class SlimmeMeterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SlimmeMeterController.class);

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final MeterstandService meterstandService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    private static final int GAS_SCALE = 3;
    private static final int STROOM_SCALE = 3;

    @Autowired
    public SlimmeMeterController(OpgenomenVermogenService opgenomenVermogenService, MeterstandService meterstandService,
            ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {

        this.opgenomenVermogenService = opgenomenVermogenService;
        this.meterstandService = meterstandService;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@RequestBody Dsmr42Reading dsmr42Reading) {
        try {
            LOGGER.info(objectMapper.writeValueAsString(dsmr42Reading));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to serialize recieved object", e);
        }

        Meterstand meterstand = mapToMeterStand(dsmr42Reading);
        meterstandService.save(meterstand);
        eventPublisher.publishEvent(new UpdateEvent(meterstand));

        OpgenomenVermogen opgenomenVermogen = mapToOpgenomenVermogen(dsmr42Reading);
        opgenomenVermogenService.save(opgenomenVermogen);
        eventPublisher.publishEvent(new UpdateEvent(opgenomenVermogen));
    }

    private Meterstand mapToMeterStand(Dsmr42Reading dsmr42Reading) {
        Meterstand meterstand = new Meterstand();
        meterstand.setDatumtijd(dsmr42Reading.getDatumtijd());
        meterstand.setStroomTariefIndicator(StroomTariefIndicator.byId(dsmr42Reading.getStroomTariefIndicator().shortValue()));
        meterstand.setGas(dsmr42Reading.getGas().setScale(GAS_SCALE, HALF_UP));
        meterstand.setStroomTarief1(dsmr42Reading.getStroomTarief1().setScale(STROOM_SCALE, HALF_UP));
        meterstand.setStroomTarief2(dsmr42Reading.getStroomTarief2().setScale(STROOM_SCALE, HALF_UP));
        return meterstand;
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(Dsmr42Reading dsmr42Reading) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(new Date(dsmr42Reading.getDatumtijd()));
        opgenomenVermogen.setWatt(dsmr42Reading.getStroomOpgenomenVermogenInWatt());
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.byId(dsmr42Reading.getStroomTariefIndicator().shortValue()));
        return opgenomenVermogen;
    }
}
