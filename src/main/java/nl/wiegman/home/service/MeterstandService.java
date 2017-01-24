package nl.wiegman.home.service;

import nl.wiegman.home.model.Meterstand;
import nl.wiegman.home.model.MeterstandOpDag;
import nl.wiegman.home.realtime.UpdateEvent;
import nl.wiegman.home.repository.MeterstandRepository;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MeterstandService {

    private static final Logger LOG = LoggerFactory.getLogger(MeterstandService.class);

    private static final int GAS_SCALE = 3;
    private static final int STROOM_SCALE = 3;

    @Autowired
    MeterstandRepository meterstandRepository;

    @Autowired
    MeterstandServiceCached meterstandServiceCached;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    public Meterstand save(Meterstand meterstand) {
        LOG.info("Save for " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(meterstand.getDatumtijd())));

        meterstand.setGas(meterstand.getGas().setScale(GAS_SCALE, RoundingMode.CEILING));
        meterstand.setStroomTarief1(meterstand.getStroomTarief1().setScale(STROOM_SCALE, RoundingMode.CEILING));
        meterstand.setStroomTarief2(meterstand.getStroomTarief2().setScale(STROOM_SCALE, RoundingMode.CEILING));

        Meterstand savedMeterstand = meterstandRepository.save(meterstand);

        eventPublisher.publishEvent(new UpdateEvent(savedMeterstand));

        return savedMeterstand;
    }

    public Meterstand getMeestRecente() {
        LOG.info("getMostRecent()");
        return meterstandRepository.getMeestRecente();
    }

    public Meterstand getOudste() {
        LOG.info("getOudste()");
        return meterstandRepository.getOudste();
    }

    public List<MeterstandOpDag> perDag(long van, long totEnMet) {
        List<MeterstandOpDag> result = new ArrayList<>();

        List<Date> dagenInPeriode = DateTimeUtil.getDagenInPeriode(van, totEnMet);
        dagenInPeriode.forEach(dag -> {
            LOG.info("Ophalen laatste meterstand op dag: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dag));

            Meterstand meterstandOpDag = getMeesteRecenteMeterstandOpDag(dag);
            result.add(new MeterstandOpDag(dag.getTime(), meterstandOpDag));
        });
        return result;
    }

    public Meterstand getOudsteMeterstandOpDag(Date dag) {
        if (DateTimeUtil.isAfterToday(dag)) {
            return null;
        } else {
            return meterstandServiceCached.getOudsteMeterstandOpDag(dag);
        }
    }

    private Meterstand getMeesteRecenteMeterstandOpDag(Date dag) {
        if (DateTimeUtil.isAfterToday(dag)) {
            return null;
        } else if (DateUtils.isSameDay(new Date(), dag)) {
            return meterstandServiceCached.getMeestRecenteMeterstandOpDag(dag);
        } else {
            return meterstandServiceCached.getPotentiallyCachedMeestRecenteMeterstandOpDag(dag);
        }
    }

    public boolean bestaatOpDatumTijd(long datumtijd) {
        return meterstandRepository.findByDatumtijd(datumtijd) != null;
    }
}
