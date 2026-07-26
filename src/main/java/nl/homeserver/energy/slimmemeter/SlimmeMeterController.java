package nl.homeserver.energy.slimmemeter;

import static java.math.RoundingMode.HALF_UP;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.homeserver.config.Paths;
import nl.homeserver.energy.StroomTariefIndicator;
import nl.homeserver.energy.meterreading.Meterstand;
import nl.homeserver.energy.meterreading.MeterstandService;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogen;
import nl.homeserver.energy.opgenomenvermogen.OpgenomenVermogenService;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Paths.API + "/slimmemeter")
@RequiredArgsConstructor
class SlimmeMeterController {

    private final OpgenomenVermogenService opgenomenVermogenService;
    private final MeterstandService meterstandService;

    private LocalDateTime lastTimeMeterReadingWasSaved = null;

    private static final int GAS_SCALE = 3;
    private static final int ELECTRICITY_SCALE = 3;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(final @RequestBody DsmrReading dsmrReading) {
        if (log.isInfoEnabled()) {
            log.info(dsmrReading.toString(), new RecursiveToStringStyle());
        }

        if (itIsTimeToStoreNewMeterReading(dsmrReading)) {
            lastTimeMeterReadingWasSaved = dsmrReading.getDatumtijd();
            saveMeterReading(dsmrReading);
            saveOpgenomenVermogen(dsmrReading);
        } else {
            log.debug("Ignoring meter reading because it is too soon after the last one. Last saved: {}, current: {}", lastTimeMeterReadingWasSaved, dsmrReading.getDatumtijd());
        }
    }

    private boolean itIsTimeToStoreNewMeterReading(final DsmrReading dsmrReading) {
        return lastTimeMeterReadingWasSaved == null || Duration.between(lastTimeMeterReadingWasSaved, dsmrReading.getDatumtijd()).getSeconds() >= 10;
    }

    private void saveMeterReading(final @RequestBody DsmrReading dsmrReading) {
        final Meterstand meterstand = mapToMeterStand(dsmrReading);
        meterstandService.save(meterstand);
    }

    private void saveOpgenomenVermogen(final @RequestBody DsmrReading dsmrReading) {
        final OpgenomenVermogen opgenomenVermogen = mapToOpgenomenVermogen(dsmrReading);
        opgenomenVermogenService.save(opgenomenVermogen);
    }

    private Meterstand mapToMeterStand(final DsmrReading dsmrReading) {
        final Meterstand meterstand = new Meterstand();
        meterstand.setDateTime(dsmrReading.getDatumtijd());
        meterstand.setStroomTariefIndicator(StroomTariefIndicator.byId(dsmrReading.getStroomTariefIndicator().shortValue()));
        meterstand.setGas(dsmrReading.getGas().setScale(GAS_SCALE, HALF_UP));
        meterstand.setStroomTarief1(dsmrReading.getStroomTarief1().setScale(ELECTRICITY_SCALE, HALF_UP));
        meterstand.setStroomTarief2(dsmrReading.getStroomTarief2().setScale(ELECTRICITY_SCALE, HALF_UP));
        meterstand.setMeterIdElectricity(dsmrReading.getMeterIdentificatieStroom());
        meterstand.setMeterIdGas(dsmrReading.getMeterIdentificatieGas());
        return meterstand;
    }

    private OpgenomenVermogen mapToOpgenomenVermogen(final DsmrReading dsmrReading) {
        final OpgenomenVermogen opgenomenVermogen = new OpgenomenVermogen();
        opgenomenVermogen.setDatumtijd(dsmrReading.getDatumtijd());
        opgenomenVermogen.setActivePowerTotalInWatts(dsmrReading.getStroomOpgenomenVermogenInWatt());
        opgenomenVermogen.setTariefIndicator(StroomTariefIndicator.byId(dsmrReading.getStroomTariefIndicator().shortValue()));
        return opgenomenVermogen;
    }
}
