package nl.wiegman.home.energie;

import java.math.RoundingMode;
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
    public void save(@RequestBody Dsmr42ReadingDto dsmr42ReadingDto) {
        try {
            LOGGER.info(objectMapper.writeValueAsString(dsmr42ReadingDto));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to serialize recieved object", e);
        }

        Meterstand meterstand = mapToMeterStand(dsmr42ReadingDto);
        meterstandService.save(meterstand);
        eventPublisher.publishEvent(new UpdateEvent(meterstand));

        OpgenomenVermogen opgenomenVermogen = mapToOpgenomenVermogen(dsmr42ReadingDto);
        opgenomenVermogenService.save(opgenomenVermogen);
        eventPublisher.publishEvent(new UpdateEvent(opgenomenVermogen));
    }

    private Meterstand mapToMeterStand(Dsmr42ReadingDto dsmr42ReadingDto) {
        Meterstand meterstand = new Meterstand();
        meterstand.setDatumtijd(dsmr42ReadingDto.getDatumtijd());
        meterstand.setStroomTariefIndicator(StroomTariefIndicator.byId(dsmr42ReadingDto.getStroomTariefIndicator().shortValue()));
        meterstand.setGas(dsmr42ReadingDto.getGas().setScale(GAS_SCALE, RoundingMode.CEILING));
        meterstand.setStroomTarief1(dsmr42ReadingDto.getStroomTarief1().setScale(STROOM_SCALE, RoundingMode.CEILING));
        meterstand.setStroomTarief2(dsmr42ReadingDto.getStroomTarief2().setScale(STROOM_SCALE, RoundingMode.CEILING));
        return meterstand;
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(Dsmr42ReadingDto dsmr42ReadingDto) {
        OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(new Date(dsmr42ReadingDto.getDatumtijd()));
        opgenomenVermogen.setWatt(dsmr42ReadingDto.getStroomOpgenomenVermogenInWatt());
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.byId(dsmr42ReadingDto.getStroomTariefIndicator().shortValue()));
        return opgenomenVermogen;
    }
}
