package nl.wiegman.home.energie;

import antlr.collections.impl.IntRange;
import nl.wiegman.home.DateTimeUtil;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class VerbruikService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbruikService.class);

    private final MeterstandService meterstandService;
    private final VerbruikServiceCached verbruikServiceCached;
    private final OpgenomenVermogenService opgenomenVermogenService;

    @Autowired
    public VerbruikService(MeterstandService meterstandService, VerbruikServiceCached verbruikServiceCached, OpgenomenVermogenService opgenomenVermogenService) {
        this.meterstandService = meterstandService;
        this.verbruikServiceCached = verbruikServiceCached;
        this.opgenomenVermogenService = opgenomenVermogenService;
    }

    public List<VerbruikInUurOpDagDto> getVerbruikPerUurOpDag(long dag) {
        return IntStream.rangeClosed(0, 23)
                .mapToObj(uur -> getVerbruikInUur(new Date(dag), uur))
                .collect(Collectors.toList());
    }

    public List<VerbruikInMaandVanJaarDto> getVerbruikPerMaandInJaar(int jaar) {
        return IntStream.rangeClosed(1, 12)
                 .mapToObj(maand -> getVerbruikInMaand(maand, jaar))
                 .collect(Collectors.toList());
    }

    public List<VerbruikOpDagDto> getVerbruikPerDag(long van, long totEnMet) {
        return DateTimeUtil.getDagenInPeriode(van, totEnMet).stream()
                .map(this::getVerbruikOpDag)
                .collect(Collectors.toList());
    }

    public List<VerbruikInJaarDto> getVerbruikPerJaar() {
        Meterstand oudste = meterstandService.getOudste();
        Meterstand nieuwste = meterstandService.getMeestRecente();

        if (oudste == null) {
            return Collections.EMPTY_LIST;
        } else {
            int jaarVan = oudste.getDatumtijdAsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().get(ChronoField.YEAR);
            int jaarTotEnMet = nieuwste.getDatumtijdAsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().get(ChronoField.YEAR);
            return IntStream.rangeClosed(jaarVan, jaarTotEnMet).mapToObj(this::getVerbruikInJaar).collect(Collectors.toList());
        }
    }

    private VerbruikInJaarDto getVerbruikInJaar(int jaar) {
        VerbruikInJaarDto verbruikInJaarDto = new VerbruikInJaarDto();
        verbruikInJaarDto.setJaar(jaar);

        try {
            long vanMillis = DateUtils.parseDate("01-01-" + jaar, "dd-MM-yyyy").getTime();
            long totEnMetMillis = DateUtils.parseDate("31-12-" + jaar, "dd-MM-yyyy").getTime() - 1;
            setVerbruik(vanMillis, totEnMetMillis, verbruikInJaarDto);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return verbruikInJaarDto;
    }

    private VerbruikInUurOpDagDto getVerbruikInUur(Date dag, int uur) {
        LOGGER.info("Get verbruik in uur " + uur + " op dag: " + new SimpleDateFormat("dd-MM-yyyy").format(dag));

        VerbruikInUurOpDagDto verbruikInUurOpDag = new VerbruikInUurOpDagDto();
        verbruikInUurOpDag.setUur(uur);

        long vanMillis = DateTimeUtil.getStartOfDay(dag) + TimeUnit.HOURS.toMillis(uur);
        long totEnMetMillis = vanMillis + TimeUnit.HOURS.toMillis(1);

        setVerbruik(vanMillis, totEnMetMillis, verbruikInUurOpDag);

        return verbruikInUurOpDag;
    }

    private VerbruikInMaandVanJaarDto getVerbruikInMaand(int maand, int jaar) {
        LOGGER.info("Get verbruik in maand: " + maand + "/" + jaar);

        VerbruikInMaandVanJaarDto verbruikInMaandVanJaar = new VerbruikInMaandVanJaarDto();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
            Date date = sdf.parse("01-" + maand + "-" + jaar);
            long vanMillis = date.getTime();
            long totEnMetMillis = DateUtils.addMilliseconds(DateUtils.addMonths(date, 1), -1).getTime();

            verbruikInMaandVanJaar.setMaand(maand);
            setVerbruik(vanMillis, totEnMetMillis, verbruikInMaandVanJaar);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return verbruikInMaandVanJaar;
    }

    private VerbruikOpDagDto getVerbruikOpDag(Date dag) {
        LOGGER.info("Get verbruik op dag: " + dag);

        long vanMillis = dag.getTime();
        long totEnMetMillis = DateUtils.addDays(dag, 1).getTime() - 1;

        VerbruikOpDagDto verbruikOpDag = new VerbruikOpDagDto();
        verbruikOpDag.setDatumtijd(vanMillis);
        setVerbruik(vanMillis, totEnMetMillis, verbruikOpDag);

        return verbruikOpDag;
    }

    private void setVerbruik(long vanMillis, long totEnMetMillis, VerbruikDto verbruik) {
        setGasVerbruik(vanMillis, totEnMetMillis, verbruik);
        setStroomVerbruikDalTarief(vanMillis, totEnMetMillis, verbruik);
        setStroomVerbruikNormaalTarief(vanMillis, totEnMetMillis, verbruik);
    }

    private void setGasVerbruik(long vanMillis, long totEnMetMillis, VerbruikDto verbruik) {
        Verbruik gasVerbruikInPeriode = getGasVerbruikInPeriode(vanMillis, totEnMetMillis);
        if (gasVerbruikInPeriode.getVerbruik() != null) {
            verbruik.setGasVerbruik(gasVerbruikInPeriode.getVerbruik());
        }
        verbruik.setGasKosten(gasVerbruikInPeriode.getKosten());
    }

    private void setStroomVerbruikDalTarief(long vanMillis, long totEnMetMillis, VerbruikDto verbruik) {
        Verbruik stroomVerbruikDalTariefInPeriode = getStroomVerbruikInPeriode(vanMillis, totEnMetMillis, StroomTariefIndicator.DAL);
        if (stroomVerbruikDalTariefInPeriode.getVerbruik() != null) {
            verbruik.setStroomVerbruikDal(stroomVerbruikDalTariefInPeriode.getVerbruik());
        }
        verbruik.setStroomKostenDal(stroomVerbruikDalTariefInPeriode.getKosten());
    }

    private void setStroomVerbruikNormaalTarief(long vanMillis, long totEnMetMillis, VerbruikDto verbruik) {
        Verbruik stroomVerbruikNormaalTariefInPeriode = getStroomVerbruikInPeriode(vanMillis, totEnMetMillis, StroomTariefIndicator.NORMAAL);
        if (stroomVerbruikNormaalTariefInPeriode.getVerbruik() != null) {
            verbruik.setStroomVerbruikNormaal(stroomVerbruikNormaalTariefInPeriode.getVerbruik());
        }
        verbruik.setStroomKostenNormaal(stroomVerbruikNormaalTariefInPeriode.getKosten());
    }

    private Verbruik getGasVerbruikInPeriode(long vanMillis, long totEnMetMillis) {
        if (totEnMetMillis < System.currentTimeMillis()) {
            return verbruikServiceCached.getPotentiallyCachedGasVerbruikInPeriode(vanMillis, totEnMetMillis);
        } else {
            return verbruikServiceCached.getGasVerbruikInPeriode(vanMillis, totEnMetMillis);
        }
    }

    private Verbruik getStroomVerbruikInPeriode(long vanMillis, long totEnMetMillis, StroomTariefIndicator stroomTariefIndicator) {
        if (totEnMetMillis < System.currentTimeMillis()) {
            return verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(vanMillis, totEnMetMillis, stroomTariefIndicator);
        } else {
            return verbruikServiceCached.getStroomVerbruikInPeriode(vanMillis, totEnMetMillis, stroomTariefIndicator);
        }
    }

    public List<OpgenomenVermogen> getOpgenomenStroomVermogenHistory(long from, long to, long subPeriodLength) {
        if (to < System.currentTimeMillis()) {
            return opgenomenVermogenService.getPotentiallyCachedOpgenomenStroomVermogenHistory(from, to, subPeriodLength);
        } else {
            return opgenomenVermogenService.getOpgenomenStroomVermogenHistory(from, to, subPeriodLength);
        }
    }
}