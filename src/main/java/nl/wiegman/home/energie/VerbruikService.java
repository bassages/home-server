package nl.wiegman.home.energie;

import static java.time.temporal.ChronoField.YEAR;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nl.wiegman.home.DateTimeUtil;

@Service
public class VerbruikService {

    private final MeterstandService meterstandService;
    private final VerbruikServiceCached verbruikServiceCached;

    @Autowired
    public VerbruikService(MeterstandService meterstandService, VerbruikServiceCached verbruikServiceCached) {
        this.meterstandService = meterstandService;
        this.verbruikServiceCached = verbruikServiceCached;
    }

    public List<VerbruikInUurOpDagDto> getVerbruikPerUurOpDag(long dag) {
        return IntStream.rangeClosed(0, 23)
                .mapToObj(uur -> getVerbruikInUur(new Date(dag), uur))
                .collect(toList());
    }

    public List<VerbruikInMaandVanJaarDto> getVerbruikPerMaandInJaar(int jaar) {
        return IntStream.rangeClosed(1, 12)
                 .mapToObj(maand -> getVerbruikInMaand(maand, jaar))
                 .collect(toList());
    }

    public List<VerbruikOpDagDto> getVerbruikPerDag(long van, long totEnMet) {
        return DateTimeUtil.getDagenInPeriode(van, totEnMet).stream()
                .map(this::getVerbruikOpDag)
                .collect(toList());
    }

    public List<VerbruikInJaarDto> getVerbruikPerJaar() {
        Meterstand oudste = meterstandService.getOudste();
        Meterstand nieuwste = meterstandService.getMeestRecente();

        if (oudste == null) {
            return emptyList();
        } else {
            int jaarVan = oudste.getDatumtijdAsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().get(YEAR);
            int jaarTotEnMet = nieuwste.getDatumtijdAsDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().get(YEAR);
            return IntStream.rangeClosed(jaarVan, jaarTotEnMet).mapToObj(this::getVerbruikInJaar).collect(toList());
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
        VerbruikInUurOpDagDto verbruikInUurOpDag = new VerbruikInUurOpDagDto();
        verbruikInUurOpDag.setUur(uur);

        long vanMillis = DateTimeUtil.getStartOfDay(dag) + TimeUnit.HOURS.toMillis(uur);
        long totEnMetMillis = vanMillis + TimeUnit.HOURS.toMillis(1);

        setVerbruik(vanMillis, totEnMetMillis, verbruikInUurOpDag);

        return verbruikInUurOpDag;
    }

    private VerbruikInMaandVanJaarDto getVerbruikInMaand(int maand, int jaar) {
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

    private Verbruik getGasVerbruikInPeriode(long periodeVanaf, long periodeTotEnMet) {
        if (periodeVanaf >= now()) {
            return new Verbruik();
        } else if (periodeTotEnMet < now()) {
            return verbruikServiceCached.getPotentiallyCachedGasVerbruikInPeriode(periodeVanaf, periodeTotEnMet);
        } else {
            return verbruikServiceCached.getGasVerbruikInPeriode(periodeVanaf, periodeTotEnMet);
        }
    }

    private Verbruik getStroomVerbruikInPeriode(long periodeVanaf, long periodeTotEnMet, StroomTariefIndicator stroomTariefIndicator) {
        if (periodeVanaf >= now()) {
            return new Verbruik();
        } else if (periodeTotEnMet < now()) {
            return verbruikServiceCached.getPotentiallyCachedStroomVerbruikInPeriode(periodeVanaf, periodeTotEnMet, stroomTariefIndicator);
        } else {
            return verbruikServiceCached.getStroomVerbruikInPeriode(periodeVanaf, periodeTotEnMet, stroomTariefIndicator);
        }
    }

    private long now() {
        return System.currentTimeMillis();
    }
}