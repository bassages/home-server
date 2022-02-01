package nl.homeserver.energie.slimmemeter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.config.Paths;
import nl.homeserver.energie.StroomTariefIndicator;
import nl.homeserver.energie.meterstand.Meterstand;
import nl.homeserver.energie.meterstand.MeterstandService;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energie.opgenomenvermogen.OpgenomenVermogenService;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static java.math.RoundingMode.HALF_UP;

@Slf4j
@RestController
@RequestMapping(Paths.API + "/slimmemeter")
@RequiredArgsConstructor
class SlimmeMeterController {

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final MeterstandService meterstandService;

    private static final int GAS_SCALE = 3;
    private static final int STROOM_SCALE = 3;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(final @RequestBody Dsmr42Reading dsmr42Reading) {
        if (log.isInfoEnabled()) {
            log.info(dsmr42Reading.toString(), new RecursiveToStringStyle());
        }

        saveMeterstand(dsmr42Reading);
        saveOpgenomenVermogen(dsmr42Reading);
    }

    private void saveMeterstand(final @RequestBody Dsmr42Reading dsmr42Reading) {
        final Meterstand meterstand = mapToMeterStand(dsmr42Reading);
        meterstandService.save(meterstand);
    }

    private void saveOpgenomenVermogen(final @RequestBody Dsmr42Reading dsmr42Reading) {
        final OpgenomenVermogen opgenomenVermogen = mapToOpgenomenVermogen(dsmr42Reading);
        opgenomenVermogenService.save(opgenomenVermogen);
    }

    private Meterstand mapToMeterStand(final Dsmr42Reading dsmr42Reading) {
        final Meterstand meterstand = new Meterstand();
        meterstand.setDateTime(dsmr42Reading.getDatumtijd());
        meterstand.setStroomTariefIndicator(StroomTariefIndicator.byId(dsmr42Reading.getStroomTariefIndicator().shortValue()));
        meterstand.setGas(dsmr42Reading.getGas().setScale(GAS_SCALE, HALF_UP));
        meterstand.setStroomTarief1(dsmr42Reading.getStroomTarief1().setScale(STROOM_SCALE, HALF_UP));
        meterstand.setStroomTarief2(dsmr42Reading.getStroomTarief2().setScale(STROOM_SCALE, HALF_UP));
        return meterstand;
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(final Dsmr42Reading dsmr42Reading) {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(dsmr42Reading.getDatumtijd());
        opgenomenVermogen.setWatt(dsmr42Reading.getStroomOpgenomenVermogenInWatt());
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.byId(dsmr42Reading.getStroomTariefIndicator().shortValue()));
        return opgenomenVermogen;
    }
}
